/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 */
package fi.vm.sade.organisaatio.business.impl;

import com.google.common.collect.Maps;
import fi.vm.sade.organisaatio.business.exception.NoVersionInKoodistoUriException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioHierarchyException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioLakkautusKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.business.exception.YtunnusException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioNimiDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoArvoDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.service.OrganisationHierarchyValidator;

import java.util.*;
import static java.util.stream.Collectors.toList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author simok
 */
@Component
public class OrganisaatioBusinessChecker {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    protected YhteystietoArvoDAO yhteystietoArvoDAO;

    @Autowired
    protected YhteystietoElementtiDAO yhteystietoElementtiDAO;

    @Autowired
    protected OrganisaatioNimiDAO organisaatioNimiDAO;

    @Autowired
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String uriWithVersionRegExp = "^.*#[0-9]+$";

    public boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    // Organisaation järkevä min ja max päivämäärä
    private final DateTime MAX_DATE = new DateTime(2030, 12, 31, 0, 0, 0, 0);
    // ei voi käyttää JodaTimea koska se generoi vertailuja sotkevia extraminuutteja java.util.Date-konversiossa
    private final Calendar MIN_DATE = new GregorianCalendar(1900, 0, 1);

    /**
     * Tarkastetaan, että nimihistorian alkupäivämäärät ovat valideja.
     *
     * @param nimet
     */
    public void checkNimihistoriaAlkupvm(List<OrganisaatioNimi> nimet) {
        Set alkuPvms = new HashSet();
        for (OrganisaatioNimi nimi : nimet) {
            if (nimi.getAlkuPvm() == null) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
            if (alkuPvms.contains(nimi.getAlkuPvm())) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
            alkuPvms.add(nimi.getAlkuPvm());
        }
    }

    /**
     * Check that given code has not been used.
     *
     * @param org
     * @return
     */
    public boolean checkLearningInstitutionCodeIsUniqueAndNotUsed(Organisaatio org) {
        List<Organisaatio> orgs = organisaatioDAO.findBy("oppilaitosKoodi", org.getOppilaitosKoodi().trim());
        if (orgs != null && orgs.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Check that given toimipistekoodi code has not been used.
     *
     * @param toimipistekoodi
     * @return
     */
    public boolean checkToimipistekoodiIsUniqueAndNotUsed(String toimipistekoodi) {
        List<Organisaatio> orgs = organisaatioDAO.findBy("toimipisteKoodi", toimipistekoodi.trim());
        if (orgs != null && orgs.size() > 0) {
            // toimipistekoodi on jo olemassa
            LOG.debug("Toimipistekoodi already exists: " + toimipistekoodi);
            return false;
        }

        return true;
    }

    /**
     * This is called when new organisaatio is saved - so there cannot be any
     * existing ytunnus.
     *
     * @param ytunnus
     */
    public void checkYtunnusIsUniqueAndNotUsed(String ytunnus) {
        if (ytunnus != null && !organisaatioDAO.isYtunnusAvailable(ytunnus)) {
            throw new YtunnusException();
        }
    }

    public void checkOrganisaatioHierarchy(Organisaatio organisaatio, String parentOid) {
        LOG.debug("checkOrganisaatioHierarchy()");

        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(rootOrganisaatioOid);
        Organisaatio parentOrg = (parentOid != null) ? this.organisaatioDAO.findByOid(parentOid) : null;
        if (validator.apply(Maps.immutableEntry(parentOrg, organisaatio))) {
            //check children
            if (organisaatio.getId() != null) { // we can have children only if we are already saved
                List<Organisaatio> children = organisaatioDAO.findChildren(organisaatio.getId());
                for (Organisaatio child : children) {
                    if (!validator.apply(Maps.immutableEntry(organisaatio, child))) {
                        throw new OrganisaatioHierarchyException();
                    }
                }
            }
        } else {
            throw new OrganisaatioHierarchyException();
        }
    }

    public void checkParentChildHierarchy(Organisaatio organisaatio, Organisaatio parentOrg) {
        LOG.debug("checkParentChildHierarchy()");

        final OrganisationHierarchyValidator validator = new OrganisationHierarchyValidator(rootOrganisaatioOid);

        if (validator.apply(Maps.immutableEntry(parentOrg, organisaatio)) == false) {
            throw new OrganisaatioHierarchyException();
        }
    }

    public void checkLakkautusAlkavatKoulutukset(Organisaatio entity) {
        if (organisaatioTarjonta.alkaviaKoulutuksia(entity.getOid(), entity.getLakkautusPvm())) {
            throw new OrganisaatioLakkautusKoulutuksiaException();
        }
    }

    public void checkVersionInKoodistoUris(Organisaatio entity) {
        // kotipaikka

        // maa
        // metadata.hakutoimistonNimi
        // metadata.data
        // kielet
        for (int i = 0; i < entity.getKielet().size(); ++i) {
            if (entity.getKielet().get(i).matches(uriWithVersionRegExp) == false) {
                LOG.warn("Version missing from koodistouri! Organisaation kieli: " + entity.getKielet().get(i));
                throw new NoVersionInKoodistoUriException();
            }
        }

        // oppilaitostyyppi
        if (isEmpty(entity.getOppilaitosTyyppi()) == false) {
            if (entity.getOppilaitosTyyppi().matches(uriWithVersionRegExp) == false) {
                LOG.warn("Version missing from koodistouri! Organisaation oppilaitostyyppi: " + entity.getOppilaitosTyyppi());
                throw new NoVersionInKoodistoUriException();
            }
        }

        // yhteystieto.postinumero
        // yhteystieto.kieli
        for (int i = 0; i < entity.getYhteystiedot().size(); ++i) {
            if (entity.getYhteystiedot().get(i).getKieli() != null) {
                if (entity.getYhteystiedot().get(i).getKieli().matches(uriWithVersionRegExp) == false) {
                    LOG.warn("Version missing from koodistouri! Organisaation yhteystiedon kieli: " + entity.getYhteystiedot().get(i).getKieli());
                    throw new NoVersionInKoodistoUriException();
                }
            }
        }

        // ryhmätyypit
        if (entity.getRyhmatyypit() != null) {
            List<String> errors = entity.getRyhmatyypit().stream().filter(t -> !t.matches(uriWithVersionRegExp)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation ryhmätyypit: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }

        // käyttöryhmät
        if (entity.getKayttoryhmat() != null) {
            List<String> errors = entity.getKayttoryhmat().stream().filter(t -> !t.matches(uriWithVersionRegExp)).collect(toList());
            if (!errors.isEmpty()) {
                LOG.warn("Version missing from koodistouri! Organisaation käyttöryhmät: {}", errors);
                throw new NoVersionInKoodistoUriException();
            }
        }
    }

    public void checkToimipisteNimiFormat(Organisaatio entity, MonikielinenTeksti parentNimi) {
        LOG.debug("checkToimipisteNimiFormat");
        MonikielinenTeksti nimi = entity.getNimi();
        for (String key : nimi.getValues().keySet()) {
            String p = parentNimi.getString(key);
            String n = nimi.getString(key);
            if (p != null && !p.isEmpty() && n != null) {
                if (!n.startsWith(p)) {
                    // TODO: Korjataanko formatti (1), palautetaan virhe (2) vai hyväksytään (3)?
                    /* 1
                     // Korjataan nimi oikeaan formaattiin
                     nimi.addString(key, n.isEmpty() ? p : p + ", " + n);
                     LOG.debug("Name[" + key + "] fixed from \"" + n + "\" to \"" + nimi.getString(key) + "\".");
                     */
                    LOG.warn("Invalid organisation name format: For toimipiste, name must be prefixed with parent name (lang:" + key
                            + ", name:" + n + ", parentname:" + p + ")");
                    /* 2
                    throw new OrganisaatioNameFormatException();
                    */
                    /* 3
                    OK
                    */
                } else {
                    // OK
                    LOG.debug("Name format OK for lang " + key);
                }
            } else {
                // TODO: Heitetäänkö poikkeus vai hyväksytäänkö?
                LOG.warn("Organisation name missing: For toimipiste, name must be given in all languages parent name exists (lang:" + key
                        + ", name:" + n + ", parentname:" + p + ")");
                //throw new OrganisaatioNameEmptyException();
            }
        }
    }

    /*
    Validate min and max dates. Check the suborganisation chain too.
    Child is now supposed to end later than the parent organisation.
     */
    public String checkPvmConstraints(Organisaatio organisaatio,
            Date minPvm, Date maxPvm, HashMap<String, OrganisaatioMuokkausTiedotDTO> muokkausTiedot) {
        LOG.debug("isPvmConstraintsOk(" + minPvm + "," + maxPvm + ") (oid:" + organisaatio.getOid() + ")");

        final Date MIN_DATE = this.MIN_DATE.getTime();
        final Date MAX_DATE = this.MAX_DATE.toDate();

        Date actualStart = organisaatio.getAlkuPvm();
        Date actualEnd = organisaatio.getLakkautusPvm();
        OrganisaatioMuokkausTiedotDTO ownData = muokkausTiedot.get(organisaatio.getOid());
        if (ownData != null) {
            // for modified data validate modification, not existing values
            LOG.debug("isPvmConstraintsOk(): omat tiedot löytyy listasta");
            actualStart = ownData.getAlkuPvm() != null ? ownData.getAlkuPvm() : MIN_DATE;
            actualEnd = ownData.getLoppuPvm() != null ? ownData.getLoppuPvm() : MAX_DATE;
            LOG.debug("uusi alku:" + actualStart + ", uusi loppu:" + actualEnd);
        } else {
            LOG.debug("isPvmConstraintsOk(): omia tietoja ei löydy");
        }
        if (actualStart == null) {
            actualStart = MIN_DATE;
        }
        if (minPvm == null) {
            minPvm = MIN_DATE;
        }
        if (actualEnd == null) {
            actualEnd = MAX_DATE;
        }
        if (maxPvm == null) {
            maxPvm = MAX_DATE;
        }
        LOG.debug(String.format("käytetty alkuPvm: %s, aikaisin sallittu alkuPvm: %s, käytetty loppuPvm: %s, myöhäisin sallittu loppuPvm: %s", actualStart, minPvm, actualEnd, maxPvm));
        if (DateTimeComparator.getDateOnlyInstance().compare(actualStart, actualEnd) > 0) {
            String virhe = String.format("oid: %s: käytetty alkuPvm (%s) > käytetty loppuPvm (%s)", organisaatio.getOid(), actualStart, actualEnd);
            LOG.error(virhe);
            return virhe;
        }
        if (DateTimeComparator.getDateOnlyInstance().compare(actualStart, minPvm) < 0) {
            String virhe = String.format("oid: %s: käytetty alkuPvm (%s) < min päivämäärä (%s)", organisaatio.getOid(), actualStart, minPvm);
            LOG.error(virhe);
            return virhe;
        }
        if (DateTimeComparator.getDateOnlyInstance().compare(actualEnd, maxPvm) > 0) {
            String virhe = String.format("oid: %s: käytetty loppuPvm (%s) > max päivämäärä (%s)", organisaatio.getOid(), actualEnd, maxPvm);
            LOG.error(virhe);
            return virhe;
        }
        for (Organisaatio child : organisaatio.getChildren(true)) {
            LOG.debug("kysytään lapselta " + child.getOid());
            String lapsenVirhe = checkPvmConstraints(child, null, actualEnd, muokkausTiedot);
            if (!lapsenVirhe.equals("")) {
                String virhe = String.format("lapsen %s virhe: %s", child.getOid(), lapsenVirhe);
                LOG.error("lapsella ajat NOK: " + lapsenVirhe);
                return virhe;
            }
        }
        LOG.debug("ajat OK");
        return "";
    }

    public DateTime getMAX_DATE() {
        return MAX_DATE;
    }

    public Calendar getMIN_DATE() {
        return MIN_DATE;
    }
}
