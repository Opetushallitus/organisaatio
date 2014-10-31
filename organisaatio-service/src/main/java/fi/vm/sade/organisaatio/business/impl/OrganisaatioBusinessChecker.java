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
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.OrganisationHierarchyValidator;

import java.util.*;
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
    private OrganisaatioKoulutukset organisaatioKoulutukset;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String uriWithVersionRegExp = "^.*#[0-9]+$";

    public boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    /**
     * Tarkastetaan, että nimihistorian alkupäivämäärät ovat valideja.
     *
     * @param nimet
     */
    public void checkNimihistoriaAlkupvm(List<OrganisaatioNimi> nimet) {
        for (OrganisaatioNimi nimi : nimet) {
            if (nimi.getAlkuPvm() == null) {
                throw new OrganisaatioNameHistoryNotValidException();
            }
        }
    }

    /**
     * Check that given code has not been used.
     *
     * @param org
     * @return
     */
    public boolean checkLearningInstitutionCodeIsUniqueAndNotUsed(OrganisaatioRDTO org) {
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

    public void checkLakkautusAlkavatKoulutukset(Organisaatio entity) {
        if (organisaatioKoulutukset.alkaviaKoulutuksia(entity.getOid(), entity.getLakkautusPvm())) {
            throw new OrganisaatioLakkautusKoulutuksiaException();
        }
    }

    public boolean isAllowed(Organisaatio org, YhteystietojenTyyppi yad) {
        if (org.getOppilaitosTyyppi() != null
                && yad.getSovellettavatOppilaitostyyppis().contains(org.getOppilaitosTyyppi())) {
            return true;
        }
        for (String otype : org.getTyypit()) {
            if (yad.getSovellettavatOrganisaatioTyyppis().contains(otype)) {
                return true;
            }
        }
        return false;
    }

    public void checkVersionInKoodistoUris(OrganisaatioRDTO model) {
        // kotipaikka

        // maa
        // metadata.hakutoimistonNimi
        // metadata.data
        // kielet
        for (int i = 0; i < model.getKieletUris().size(); ++i) {
            if (model.getKieletUris().get(i).matches(uriWithVersionRegExp) == false) {
                LOG.warn("Version missing from koodistouri! Organisaation kieli: " + model.getKieletUris().get(i));
                throw new NoVersionInKoodistoUriException();
            }
        }

        // oppilaitostyyppi
        if (isEmpty(model.getOppilaitosTyyppiUri()) == false) {
            if (model.getOppilaitosTyyppiUri().matches(uriWithVersionRegExp) == false) {
                LOG.warn("Version missing from koodistouri! Organisaation oppilaitostyyppi: " + model.getOppilaitosTyyppiUri());
                throw new NoVersionInKoodistoUriException();
            }
        }

        // yhteystieto.postinumero
        // yhteystieto.kieli
        for (int i = 0; i < model.getYhteystiedot().size(); ++i) {
            if (model.getYhteystiedot().get(i).containsKey("kieli")) {
                if (model.getYhteystiedot().get(i).get("kieli").matches(uriWithVersionRegExp) == false) {
                    LOG.warn("Version missing from koodistouri! Organisaation yhteystiedon kieli: " + model.getYhteystiedot().get(i).get("kieli"));
                    throw new NoVersionInKoodistoUriException();
                }
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
}
