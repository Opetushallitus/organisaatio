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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.OrganisaatioValidationConstraints;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.exception.*;
import fi.vm.sade.organisaatio.dao.*;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTulosListaDTO;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioNimiDTOV2;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.OrganisationDateValidator;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.service.YtjDtoMapperHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author simok
 */
@Transactional
@Service("organisaatioBusinessService")
public class OrganisaatioBusinessServiceImpl implements OrganisaatioBusinessService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioSuhdeDAO organisaatioSuhdeDAO;

    @Autowired
    protected YhteystietoArvoDAO yhteystietoArvoDAO;

    @Autowired
    private YhteystietojenTyyppiDAO yhteystietojenTyyppiDAO;

    @Autowired
    protected YhteystietoElementtiDAO yhteystietoElementtiDAO;

    @Autowired
    protected OrganisaatioNimiDAO organisaatioNimiDAO;

    @Autowired
    private OrganisaatioNimiModelMapper organisaatioNimiModelMapper;

    @Autowired
    private OrganisaatioBusinessChecker checker;

    @Autowired
    private IndexerResource solrIndexer;

    @Autowired
    private YTJResource ytjResource;

    @Autowired
    private OIDService oidService;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioTarjonta organisaatioTarjonta;

    @Autowired
    private OrganisaatioKoodisto organisaatioKoodisto;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String parentSeparator = "|";

    public static final String KIELI_KOODI_FI = "kieli_fi#1";
    public static final String KIELI_KOODI_SV = "kieli_sv#1";
    public static final String ORG_KIELI_KOODI_FI = "oppilaitoksenopetuskieli_1#1";
    public static final String ORG_KIELI_KOODI_SV = "oppilaitoksenopetuskieli_2#1";

    private void mergeAuxData(Organisaatio entity, Organisaatio orgEntity) {
        try {
            if (orgEntity.getKuvaus2() != null) {
                entity.getKuvaus2().setId(orgEntity.getKuvaus2().getId());
                entity.getKuvaus2().setVersion(orgEntity.getKuvaus2().getVersion());
            }
            OrganisaatioMetaData metadata = entity.getMetadata();
            OrganisaatioMetaData orgMetadata = orgEntity.getMetadata();
            if (metadata != null && orgMetadata != null) {
                metadata.setId(orgMetadata.getId());
                metadata.setVersion(orgMetadata.getVersion());
                if (orgMetadata.getHakutoimistoNimi() != null) {
                    metadata.getHakutoimistoNimi().setId(orgMetadata.getHakutoimistoNimi().getId());
                    metadata.getHakutoimistoNimi().setVersion(orgMetadata.getHakutoimistoNimi().getVersion());
                }
                if (orgMetadata.getNimi() != null) {
                    metadata.getNimi().setId(orgMetadata.getNimi().getId());
                    metadata.getNimi().setVersion(orgMetadata.getNimi().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsNimi() != null) {
                    metadata.getHakutoimistoEctsNimi().setId(orgMetadata.getHakutoimistoEctsNimi().getId());
                    metadata.getHakutoimistoEctsNimi().setVersion(orgMetadata.getHakutoimistoEctsNimi().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsEmail() != null) {
                    metadata.getHakutoimistoEctsEmail().setId(orgMetadata.getHakutoimistoEctsEmail().getId());
                    metadata.getHakutoimistoEctsEmail().setVersion(orgMetadata.getHakutoimistoEctsEmail().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsPuhelin() != null) {
                    metadata.getHakutoimistoEctsPuhelin().setId(orgMetadata.getHakutoimistoEctsPuhelin().getId());
                    metadata.getHakutoimistoEctsPuhelin().setVersion(orgMetadata.getHakutoimistoEctsPuhelin().getVersion());
                }
                if (orgMetadata.getHakutoimistoEctsTehtavanimike() != null) {
                    metadata.getHakutoimistoEctsTehtavanimike().setId(orgMetadata.getHakutoimistoEctsTehtavanimike().getId());
                    metadata.getHakutoimistoEctsTehtavanimike().setVersion(orgMetadata.getHakutoimistoEctsTehtavanimike().getVersion());
                }
                for (NamedMonikielinenTeksti value : metadata.getValues()) {
                    MonikielinenTeksti mkt = orgMetadata.getNamedValue(value.getKey());
                    if (mkt != null) {
                        value.getValue().setId(mkt.getId());
                        value.getValue().setVersion(mkt.getVersion());
                    }
                }
            }
        } catch (Exception ex) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage(), "organisaatio.error.merge.aux.data");
        }
    }

    private String getCurrentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public OrganisaatioResult save(OrganisaatioRDTO model, boolean updating, boolean skipParentDateValidation) throws ValidationException {
        // Tarkistetaan OID
        if (model.getOid() == null && updating) {
            throw new ValidationException("Oid cannot be null");//trying to update organisaatio that doesn't exist (is is null)");
        } else if (!updating) {
            if ((model.getOid() != null) && (organisaatioDAO.findByOid(model.getOid()) != null)) {
                throw new OrganisaatioExistsException(model.getOid());
            }

            if (model.getOppilaitosKoodi() != null && model.getOppilaitosKoodi().length() > 0) {
                if (checker.checkLearningInstitutionCodeIsUniqueAndNotUsed(model)) {
                    throw new LearningInstitutionExistsException("organisaatio.oppilaitos.exists.with.code");
                }
            }
        }

        // Haetaan parent organisaatio
        Organisaatio parentOrg = (model.getParentOid() != null && !model.getParentOid().equalsIgnoreCase(rootOrganisaatioOid))
                ? organisaatioDAO.findByOid(model.getParentOid()) : null;

        // Validointi: Tarkistetaan, että parent ei ole ryhmä
        if (parentOrg != null && OrganisaatioUtil.isRyhma(parentOrg)) {
            throw new ValidationException("Parent cannot be group");
        }

        // Validointi: Tarkistetaan, että ryhmää ei olla lisäämässä muulle kuin oph organisaatiolle
        if (OrganisaatioUtil.isRyhma(model) && model.getParentOid().equalsIgnoreCase(rootOrganisaatioOid) == false) {
            throw new ValidationException("Ryhmiä ei voi luoda muille kuin oph organisaatiolle");
        }

        // Validointi: Jos organisaatio on ryhmä, tarkistetaan ettei muita ryhmiä
        if (OrganisaatioUtil.isRyhma(model) && model.getTyypit().size() != 1) {
            throw new ValidationException("Rymällä ei voi olla muita tyyppejä");
        }

        // Validointi: Jos y-tunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getYTunnus() != null && model.getYTunnus().length() == 0) {
            model.setYTunnus(null);
        }
        if (model.getYTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.YTUNNUS_PATTERN, model.getYTunnus())) {
            throw new ValidationException("validation.Organisaatio.ytunnus");
        }

        // Validointi: Jos virastotunnus on annettu, sen täytyy olla oikeassa muodossa
        if (model.getVirastoTunnus() != null && model.getVirastoTunnus().length() == 0) {
            model.setVirastoTunnus(null);
        }
        if (model.getVirastoTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.VIRASTOTUNNUS_PATTERN, model.getVirastoTunnus())) {
            throw new ValidationException("validation.Organisaatio.virastotunnus");
        }

        // Validointi: koodistoureissa pitää olla versiotieto
        checker.checkVersionInKoodistoUris(model);

        Map<String, String> oldName = null;
        if (updating) {
            Organisaatio oldOrg = organisaatioDAO.findByOid(model.getOid());
            if(oldOrg.isOrganisaatioPoistettu()) {
                throw new ValidationException("validation.Organisaatio.poistettu");
            }
            oldName = new HashMap<>(oldOrg.getNimi().getValues());
        }

        // Luodaan tallennettava entity objekti
        Organisaatio entity = conversionService.convert(model, Organisaatio.class); //this entity is populated with new data

        // Asetetaan parent path
        setParentPath(entity, model.getParentOid());

        // Tarkistetaan että toimipisteen nimi on oikeassa formaatissa
        if (parentOrg != null && (organisaatioIsOfType(entity, OrganisaatioTyyppi.TOIMIPISTE)
                || organisaatioIsOfType(entity, OrganisaatioTyyppi.OPPISOPIMUSTOIMIPISTE)) &&
                !organisaatioIsOfType(entity, OrganisaatioTyyppi.OPPILAITOS)) {
            checker.checkToimipisteNimiFormat(entity, parentOrg.getNimi());
        }

        // Asetetaan päivittäjä ja päivityksen aikaleima
        try {
            entity.setPaivittaja(getCurrentUser());
            entity.setPaivitysPvm(new Date());
        } catch (Throwable t) {
            LOG.error("Could not set updater for organisation!", t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, t.getMessage(), "error.setting.updater");
        }

        // Päivitystapauksessa pitaa asetta id:t, ettei luoda uusia rivejä
        boolean parentChanged = false;
        Organisaatio oldParent = null;
        if (updating) {
            Organisaatio orgEntity = this.organisaatioDAO.findByOid(model.getOid());
            mergeAuxData(entity, orgEntity);
            entity.setId(orgEntity.getId());
            entity.setOpetuspisteenJarjNro(orgEntity.getOpetuspisteenJarjNro());

            // Tarkistetaan organisaatiohierarkia jos hierarkia muuttunut (onko parent muuttunut)
            if (model.getParentOid().equals(orgEntity.getParent().getOid()) == false) {
                LOG.info("Hierarkia muuttunut, tarkastetaan hierarkia.");
                checker.checkOrganisaatioHierarchy(entity, model.getParentOid());
                parentChanged = true;
                oldParent = orgEntity.getParent();
            }

            // Tarkistetaan organisaatiohierarkia jos organisaatiotyypit muutuneet
            if (!entity.getTyypit().containsAll(orgEntity.getTyypit())
                    || !orgEntity.getTyypit().containsAll(entity.getTyypit())) {
                LOG.info("Organisaation tyypit muuttuneet, tarkastetaan hierarkia.");
                checker.checkOrganisaatioHierarchy(entity, model.getParentOid());
            }

            // Tarkistetaan ettei lakkautuspäivämäärän jälkeen ole alkavia koulutuksia
            if (OrganisaatioUtil.isSameDay(entity.getLakkautusPvm(), orgEntity.getLakkautusPvm()) == false) {
                LOG.info("Lakkautuspäivämäärä muuttunut, tarkastetaan alkavat koulutukset.");
                checker.checkLakkautusAlkavatKoulutukset(entity);
            }
        } else {
            // Tarkistetaan organisaatio hierarkia
            checker.checkOrganisaatioHierarchy(entity, model.getParentOid());
        }

        // Generoidaan oidit
        try {
            generateOids(entity);
            generateOidsMetadata(entity.getMetadata());
        } catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, em.getMessage());
        }

        // Generoidaan opetuspiteenJarjNro
        String opJarjNro = null;
        if (!updating && StringUtils.isEmpty(model.getOpetuspisteenJarjNro())) {
            opJarjNro = generateOpetuspisteenJarjNro(entity, parentOrg, model.getTyypit());
            entity.setOpetuspisteenJarjNro(opJarjNro);
        } else {
            opJarjNro = entity.getOpetuspisteenJarjNro();
        }

        // If inserting, check if ytunnus allready exists in the database
        if (!updating && entity.getYtunnus() != null) {
            checker.checkYtunnusIsUniqueAndNotUsed(entity.getYtunnus());
        }

        entity.setOrganisaatioPoistettu(false);

        // OVT-4765 do not validate start date against parent date when updating
        if (updating) {
            LOG.info("this is an update, not validating parent dates.");
            skipParentDateValidation = true;
        }

        // OH-116
        if (parentOrg != null) {
            // Check if organization has parent and if it has check that passivation dates match to parent
            OrganisationDateValidator dateValidator = new OrganisationDateValidator(skipParentDateValidation);
            if (!dateValidator.apply(Maps.immutableEntry(parentOrg, entity))) {
                throw new OrganisaatioDateException();
            }
        }

        // Asetetaan yhteystietoarvot
        entity.setYhteystietoArvos(mergeYhteystietoArvos(entity, entity.getYhteystietoArvos(), updating));

        // Kirjoitetaan yhteystiedot uusiksi (ei päivitetä vanhoja)
        for (Yhteystieto yhtTieto : entity.getYhteystiedot()) {
            yhtTieto.setOrganisaatio(entity);
        }

        // Kirjoitetaan nimihistoria uusiksi (ei päivitetä vanhoja)
        for (OrganisaatioNimi nimi : entity.getNimet()) {
            nimi.setOrganisaatio(entity);
        }

        // Nimihistoriaan liittyvät tarkistukset (HUOM! Ei koske Ryhmiä)
        if (OrganisaatioUtil.isRyhma(entity) == false) {
            /** @TODO --> Tarkistetaan, ettei nimihistoriaa muuteta muuta kuin nykyisen tai uusimman nimen osalta */
            // Tarkistetaan, että nimen alkupäivämäärä ei ole NULL
            checker.checkNimihistoriaAlkupvm(entity.getNimet());

            // Tarkistetaan, että nimihistoriassa on organisaatiolle validi nimi
            MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(entity.getNimet());
            if (nimi == null) {
                throw new OrganisaatioNameHistoryNotValidException();
            }

            // Tarkistetaan, että organisaatiolle asetettu nimi ei ole
            // ristiriidassa nimihistorian kanssa
            if (nimi.getValues().equals(entity.getNimi().getValues()) == false) {
                throw new OrganisaatioNameHistoryNotValidException();
            }

            // Asetetaan organisaatiolle sama nimi instanssi kuin nimihistoriassa
            entity.setNimi(nimi);
        }

        // Asetetaan tyypit "organisaatio" taulun kenttään
        String tyypitStr = "";
        for (String curTyyppi : model.getTyypit()) {
            tyypitStr += curTyyppi + "|";
        }
        entity.setOrganisaatiotyypitStr(tyypitStr);

        // Generate natural key, OVT-4954
        // "Jos kyseessä on koulutustoimija pitäisi palauttaa y-tunnus."
        // "Jos oppilaitos, palautetaan oppilaitosnumero."
        // "Jos toimipiste, palautetaan oppilaitosnro+toimipisteenjärjestysnumero(konkatenoituna)sekä yhkoulukoodi."
        entity.setToimipisteKoodi(calculateToimipisteKoodi(entity, parentOrg));

        // call super.insert OR update which saves & validates jpa
        if (updating) {
            LOG.info("updating " + entity);
            try {
                organisaatioDAO.update(entity);
            } catch (OptimisticLockException ole) {
                throw new OrganisaatioModifiedException(ole);
            }
            entity = organisaatioDAO.read(entity.getId());
        } else {
            entity = organisaatioDAO.insert(entity);
        }

        // Saving the parent relationship
        if (parentOrg == null) {
            // Koulutustoimija in root level is stored under OPH
            Organisaatio uberParent = organisaatioDAO.findByOid(rootOrganisaatioOid);
            entity = saveParentSuhde(entity, uberParent, opJarjNro);
        } else {
            entity = saveParentSuhde(entity, parentOrg, opJarjNro);
            if (!updating && entity.getParent() != null) {
                solrIndexer.index(Arrays.asList(parentOrg));
            }
        }

        // Indeksoidaan organisaatio solriin (HUOM! Ryhmiä ei indeksoida)
        // Uuden organisaation tapauksessa uudelleenindeksoidaan myös parent
        if (OrganisaatioUtil.isRyhma(entity) == false) {
            solrIndexer.index(entity);

            if ((parentChanged || !updating) && parentOrg != null) {
                solrIndexer.index(parentOrg);
            }
        }

        // Tarkistetaan ja päivitetään oppilaitoksen alla olevien opetuspisteiden nimet
        if (updating && parentOrg != null && organisaatioIsOfType(entity, OrganisaatioTyyppi.OPPILAITOS)) {
            updateOrganisaatioNameHierarchy(entity, oldName);
        }

        // Parent changed update children and reindex old parent.
        if (parentChanged) {
            updateChildrenRecursive(entity);
            solrIndexer.index(oldParent);
        }

        // Päivitä tiedot koodistoon.
        String info = organisaatioKoodisto.paivitaKoodisto(entity, true);

        return new OrganisaatioResult(entity, info);
    }

    private Organisaatio saveParentSuhde(Organisaatio child, Organisaatio parent, String opJarjNro) {
        OrganisaatioSuhde curSuhde = organisaatioSuhdeDAO.findParentTo(child.getId(), null);
        if (parent != null && (curSuhde == null || curSuhde.getParent().getId().equals(parent.getId()) == false)) {
            if (curSuhde != null) {
                // Set end date for current parent relation before create new one.
                curSuhde.setLoppuPvm(new Date());
                organisaatioSuhdeDAO.update(curSuhde);
            }
            organisaatioSuhdeDAO.addChild(parent.getId(), child.getId(), Calendar.getInstance().getTime(), opJarjNro);
        }
        child.setParentSuhteet(organisaatioSuhdeDAO.findBy("child", child));
        return this.organisaatioDAO.findByOid(child.getOid());
    }

    private List<YhteystietoArvo> mergeYhteystietoArvos(Organisaatio org, List<YhteystietoArvo> nys,
            boolean updating) {

        Map<String, YhteystietoArvo> ov = new HashMap<>();

        for (YhteystietoArvo ya : yhteystietoArvoDAO.findByOrganisaatio(org)) {
            if (!isAllowed(org, ya.getKentta().getYhteystietojenTyyppi())) {
                yhteystietoArvoDAO.remove(ya);
            } else {
                ov.put(ya.getKentta().getOid() + ya.getKieli(), ya);
            }
        }

        List<YhteystietoArvo> ret = new ArrayList<>();

        for (YhteystietoArvo ya : nys) {
            List<YhteystietojenTyyppi> yt = yhteystietojenTyyppiDAO.findBy("oid", ya.getKentta().getYhteystietojenTyyppi().getOid());
            if (yt.isEmpty()) {
                continue;
            }
            List<YhteystietoElementti> kentat = yhteystietoElementtiDAO.findBy("oid", ya.getKentta().getOid());
            if (kentat.isEmpty()) {
                continue;
            }
            ya.setKentta(kentat.get(0));
            if (!isAllowed(org, yt.get(0))) {
                continue;
            }
            YhteystietoArvo o = ov.get(ya.getKentta().getOid() + ya.getKieli());
            if (o != null) {
                o.setArvoText(ya.getArvoText());
                o.setKieli(ya.getKieli());
                yhteystietoArvoDAO.update(o);
                ret.add(o);
            } else {
                ya.setOrganisaatio(org);
                try {
                    ya.setYhteystietoArvoOid(oidService.newOid(NodeClassCode.TEKN_5));
                } catch (ExceptionMessage em) {
                    throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, em.getMessage());
                }
                if (updating) {
                    yhteystietoArvoDAO.insert(ya);
                }
                ret.add(ya);
            }
        }

        return ret;
    }

    private String calculateToimipisteKoodi(Organisaatio org) {
        return calculateToimipisteKoodi(org, org.getParent());
    }

    /**
     * Lasketaan opetuspisteen / toimipisteen koodi.
     * Lisätään parent oppilaitoksen oppilaitoskoodiin opetuspisteen järjestysnumero.
     *
     * @param org
     * @return
     */
    private String calculateToimipisteKoodi(Organisaatio org, Organisaatio parent) {
        LOG.debug("calculateToimipisteKoodi(org={})", org);

        if (org == null) {
            LOG.warn("  org  == null, return ''");
            return "";
        }

        if (organisaatioIsOfType(org, OrganisaatioTyyppi.OPPILAITOS)) {
            LOG.debug("  org  == OPPILAITOS, return oppilaitoskoodi: '{}'", org.getOppilaitosKoodi());
            return org.getOppilaitosKoodi();
        }

        if (organisaatioIsOfType(org, OrganisaatioTyyppi.TOIMIPISTE)) {
            LOG.debug("  org  == TOIMIPISTE, return parent opk/olk code AND this ones order number: '{}'", org.getOpetuspisteenJarjNro());

            Organisaatio parentOppilaitos = findParentOppilaitos(org, parent);
            if (parentOppilaitos == null) {
                LOG.warn("Oppilaitos not found in parents");
                return null;
            }

            String opJarjNro = org.getOpetuspisteenJarjNro();
            if (isEmpty(opJarjNro)) {
                LOG.warn("Organisaatiolta {} puuttuu opetuspisteen järjestysnumero, return ''", org.getOid());
                return "";
            }

            return parentOppilaitos.getOppilaitosKoodi() + opJarjNro;
        }

        LOG.debug("calculateToimipisteKoodi == TYPE unknown?: types='{}'", org.getTyypit());

        return "";
    }

    /**
     * Check given organisation type.
     *
     * @param org
     * @param organisaatioTyyppi
     * @return
     */
    private boolean organisaatioIsOfType(Organisaatio org, OrganisaatioTyyppi organisaatioTyyppi) {
        if (organisaatioTyyppi == null || org == null) {
            return false;
        }

        return (org.getTyypit() != null) && (org.getTyypit().contains(organisaatioTyyppi.value()));
    }

    private void generateOids(Organisaatio organisaatio) throws ExceptionMessage {
        if (organisaatio.getOid() == null) {
            if (OrganisaatioUtil.isRyhma(organisaatio)) {
                /// @TODO: Sitten kun Oid palveluss on ryhmä mukana --> korjaa
                //  organisaatio.setOid(oidService.newOid(NodeClassCode.RYHMA));

                organisaatio.setOid(oidService.newOidByClassValue("28"));
            } else {
                organisaatio.setOid(oidService.newOid(NodeClassCode.TOIMIPAIKAT));
            }
        }
        for (Yhteystieto curYt : organisaatio.getYhteystiedot()) {
            if (curYt.getYhteystietoOid() == null) {
                curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
    }

    private void generateOidsMetadata(OrganisaatioMetaData omd) throws ExceptionMessage {
        if (omd != null) {
            for (Yhteystieto curYt : omd.getYhteystiedot()) {
                if (curYt != null && curYt.getYhteystietoOid() == null) {
                    curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
                }
            }
        }
    }

    private String generateOpetuspisteenJarjNro(Organisaatio entity) {
        return generateOpetuspisteenJarjNro(entity, entity.getParent(), entity.getTyypit());
    }

    /*
     * Generoidaan opetuspisteen järjestysnumero toimipisteelle.
     * Opetuspisteen järjestysnumero on seuraava vapaa numero oppilaitoksen alla.
     */
    private String generateOpetuspisteenJarjNro(Organisaatio entity, Organisaatio parent, List<String> tyypit) {
        // Opetuspisteen jarjestysnumero generoidaan vain toimipisteille,
        // mutta jos organisaatio on samalla oppilaitos, niin ei generoida
        if (tyypit.contains(OrganisaatioTyyppi.OPPILAITOS.value())
                && !tyypit.contains(OrganisaatioTyyppi.TOIMIPISTE.value())) {
            LOG.debug("Organisaatio {} ei toimipiste -> ei tarvetta opetuspisteen järjestysnumerolle ({})",
                    entity.getOid(), tyypit);
            return null;
        }

        // Haetaan parent oppilaitos
        Organisaatio parentOppilaitos = findParentOppilaitos(entity, parent);
        if (parentOppilaitos == null) {
            LOG.warn("Oppilaitos not found in parents");
            return null;
        }

        // Kokeillaan aina seuraavaa numeroa kunnes vapaa toimipistekoodi löytyy
        String jarjNro;
        int nextVal = parentOppilaitos.getChildCount(new Date()) + 1;
        for (int i = nextVal; i < 100; i++) {
            jarjNro = (i < 10) ? String.format("%s%s", "0", i) : String.format("%s", i);
            if (checker.checkToimipistekoodiIsUniqueAndNotUsed(parentOppilaitos.getOppilaitosKoodi() + jarjNro)) {
                LOG.debug("Generoitu opetuspisteen järjestysnumero: {} / {}", parentOppilaitos.getOppilaitosKoodi(), jarjNro);
                return jarjNro;
            }
        }

        LOG.warn("Failed to generate opetuspisteenjarjnro (oppilaitoskoodi=" + parentOppilaitos.getOppilaitosKoodi() + ")");
        return null;

    }

    private void setParentPath(Organisaatio entity, String parentOid) {
        if (parentOid == null) {
            parentOid = rootOrganisaatioOid;
        }
        String parentOidPath = "";
        String parentIdPath = "";
        List<Organisaatio> parents = organisaatioDAO.findParentsTo(parentOid);
        for (Organisaatio curParent : parents) {
            parentOidPath += parentSeparator + curParent.getOid();
            parentIdPath += parentSeparator + curParent.getId();
        }
        if (!parents.isEmpty()) {
            parentOidPath += parentSeparator;
            parentIdPath += parentSeparator;
        }
        entity.setParentOidPath(parentOidPath);
        entity.setParentIdPath(parentIdPath);
    }

    private Organisaatio findParentOppilaitos(Organisaatio organisaatio, Organisaatio parent) {
        Organisaatio currentParent = organisaatio.getParent();

        // Uuden organisaation luonnin yhteydessä ei parent-suhdetta ole välttämättä vielä
        // asetettu.
        if (currentParent == null) {
            currentParent = parent;
        }

        while (currentParent != null) {
            if (OrganisaatioUtil.isOppilaitos(currentParent)) {
                return currentParent;
            }

            currentParent = currentParent.getParent();
        }
        return null;
    }

    private boolean isAllowed(Organisaatio org, YhteystietojenTyyppi yad) {
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

    private boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    private void updateChildrenRecursive(Organisaatio parent) {
        List<Organisaatio> children = organisaatioDAO.findChildren(parent.getId());
        if (children == null || children.isEmpty()) {
            return;
        }

        for (Organisaatio child : children) {
            // Create new parent id / oid paths for child.
            setParentPath(child, parent.getOid());
            organisaatioDAO.update(child);
            updateChildrenRecursive(child);
        }
        solrIndexer.index(children);
    }

    private void updateOrganisaatioNameHierarchy(Organisaatio oppilaitos, Map<String, String> oldName) {
        updateOrganisaatioNameHierarchy(oppilaitos, oldName, true);
    }

    private void updateOrganisaatioNameHierarchy(Organisaatio oppilaitos, Map<String, String> oldName, boolean updatePaivittaja) {
        LOG.debug("updateOrganisaatioNameHierarchy()");

        if (oppilaitos.getId() != null) {
            List<Organisaatio> children = organisaatioDAO.findChildren(oppilaitos.getId());
            MonikielinenTeksti newParentNames = oppilaitos.getNimi();
            boolean childrenChanged = false;
            for (Organisaatio child : children) {
                MonikielinenTeksti childnimi = child.getNimi();
                boolean childChanged = false;
                for (String key : oldName.keySet()) {
                    String oldParentName = oldName.get(key);
                    String oldChildName = childnimi.getString(key);
                    String newParentName = newParentNames.getString(key);
                    if (oldChildName == null) {
                        // toimipisteellä ei ole oppilaitosta vastaavaa tämänkielistä nimeä
                        // Pitää lisätä manuaalisesti
                        LOG.debug("Name[" + key + "] does not exist.");
                        childChanged = true;
                    } else if (newParentName == null) {
                        // oppilaitoksen nimi poistettu, ei muuteta toimipisteen nimeä
                    } else if (oldChildName.startsWith(oldParentName)) {
                        // päivitetään toimipisteen nimen alkuosa
                        childnimi.addString(key, oldChildName.replace(oldChildName.substring(0, oldParentName.length()), newParentName));

                        // Päivitetään organisaation päivittäjän tiedot
                        if (updatePaivittaja) {
                            try {
                                child.setPaivittaja(getCurrentUser());
                                child.setPaivitysPvm(new Date());
                            } catch (Throwable t) {
                                throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, t.getMessage(), "error.setting.updater");
                            }
                        }
                        organisaatioDAO.update(child);
                        childChanged = true;
                        childrenChanged = true;
                        LOG.debug("Name[" + key + "] updated to \"" + childnimi.getString(key) + "\".");
                    } else {
                        // nimen formaatti on muu kuin "oppilaitoksennimi, toimipisteennimi"
                        // Pitää korjata formaatti manuaalisesti
                        LOG.debug("Name[" + key + "] is of invalid format: \"" + childnimi.getString(key) + "\".");
                        childChanged = true;
                    }
                }
                if (childChanged == true) {
                    organisaatioKoodisto.paivitaKoodisto(child, false);
                }
            }
            if (childrenChanged == true) {
                solrIndexer.index(children);
            }
        }
    }

    @Override
    public List<OrganisaatioNimi> getOrganisaatioNimet(String oid) {
        try {
            return organisaatioNimiDAO.findNimet(oid);
        }
        catch (IllegalArgumentException ex) {
            throw new OrganisaatioNotFoundException(oid);
        }
    }

    @Override
    public OrganisaatioNimi newOrganisaatioNimi(String oid, OrganisaatioNimiDTOV2 nimidto) {
        Organisaatio orgEntity = this.organisaatioDAO.findByOid(oid);

        if (orgEntity == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        // Luodaan tallennettava entity objekti
        OrganisaatioNimi nimiEntity = organisaatioNimiModelMapper.map(nimidto, OrganisaatioNimi.class);

        // Asetetaan organisaatio
        nimiEntity.setOrganisaatio(orgEntity);

        // Insertoidaan kantaan
        nimiEntity = organisaatioNimiDAO.insert(nimiEntity);

        // Jos nimi tulee nykyiseksi nimeksi, niin päivitetään se myös organisaatioon.
        if (OrganisaatioNimiUtil.isValidCurrentNimi(nimiEntity)) {
            // Asetetaan organisaation nimi ja nimihistorian nykyinen nimi
            // osoittamaan varmasti samaan monikieliseen tekstiin
            orgEntity.setNimi(nimiEntity.getNimi());

            LOG.info("updating " + orgEntity);
            try {
                organisaatioDAO.update(orgEntity);
            } catch (OptimisticLockException ole) {
                throw new OrganisaatioModifiedException(ole);
            }

            // Indeksoidaan organisaatio solriin uudella nimellä
            solrIndexer.index(Lists.newArrayList(orgEntity));
        }

        return nimiEntity;
    }

    @Override
    public OrganisaatioNimi updateOrganisaatioNimi(String oid, Date alkuPvm, OrganisaatioNimiDTOV2 nimidto) {
        Organisaatio orgEntity = this.organisaatioDAO.findByOid(oid);

        if (orgEntity == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        LOG.debug("Haetaan organisaation: " + oid + " nimeä alkupäivämäärällä: " + alkuPvm);

        // Haetaan päivitettävä entity objecti
        OrganisaatioNimi nimiEntityOld = this.organisaatioNimiDAO.findNimi(orgEntity, alkuPvm);

        if (nimiEntityOld == null) {
            throw new OrganisaatioNimiNotFoundException(oid, alkuPvm);
        }

        // Luodaan tallennettava entity objekti
        OrganisaatioNimi nimiEntityNew = organisaatioNimiModelMapper.map(nimidto, OrganisaatioNimi.class);

        // Asetetaan organisaatio
        nimiEntityNew.setOrganisaatio(orgEntity);

        // Päivitystapauksessa pitaa asetta id:t, ettei luoda uusia rivejä
        nimiEntityNew.setId(nimiEntityOld.getId());
        nimiEntityNew.getNimi().setId(nimiEntityOld.getNimi().getId());
        nimiEntityNew.getNimi().setVersion(nimiEntityOld.getNimi().getVersion());

        LOG.info("updating " + nimiEntityNew);
        try {
            // Päivitetään nimi
            organisaatioNimiDAO.update(nimiEntityNew);
        } catch (OptimisticLockException ole) {
            throw new OrganisaatioNimiModifiedException(ole);
        }

        // Palautetaan päivitetty nini
        nimiEntityNew = organisaatioNimiDAO.read(nimiEntityNew.getId());

        return nimiEntityNew;
    }

    @Override
    public void deleteOrganisaatioNimi(String oid, Date alkuPvm) {
        Organisaatio orgEntity = this.organisaatioDAO.findByOid(oid);

        if (orgEntity == null) {
            throw new OrganisaatioNotFoundException(oid);
        }

        // Haetaan poistettava entity objecti
        OrganisaatioNimi nimiEntity = this.organisaatioNimiDAO.findNimi(orgEntity, alkuPvm);

        // Tarkistetaan, että nimi ei ole nykyinen nimi
        OrganisaatioNimi currentNimiEntity = this.organisaatioNimiDAO.findCurrentNimi(orgEntity);

        if (nimiEntity == null) {
            throw new OrganisaatioNimiNotFoundException(oid, alkuPvm);
        }

        // Tarkistetaan ettei poistettava nimi ole organisaation nykyinen nimi
        if (currentNimiEntity != null) {
            if (currentNimiEntity.getId().equals(nimiEntity.getId())) {
                throw new OrganisaatioNimiDeleteException();
            }
        }

        // Vain uusimman nimen, jonka voimassaolo ei ole alkanut saa poistaa
        if (alkuPvm.before(new Date())) {
            throw new OrganisaatioNimiDeleteException();
        }

        LOG.info("deleting " + nimiEntity);

        // Poistetaan
        this.organisaatioNimiDAO.remove(nimiEntity);
    }

    @Override
    public OrganisaatioMuokkausTulosListaDTO bulkUpdatePvm(List<OrganisaatioMuokkausTiedotDTO> tiedot) {
        LOG.debug("bulkUpdatePvm():" + tiedot);
        OrganisaatioMuokkausTulosListaDTO edited = new OrganisaatioMuokkausTulosListaDTO(tiedot.size());

        HashMap<String, OrganisaatioMuokkausTiedotDTO> givenData = new HashMap<>(tiedot.size());
        HashMap<String, Organisaatio> organisaatioMap = new HashMap<>(tiedot.size());

        for(OrganisaatioMuokkausTiedotDTO tieto:tiedot) {
            givenData.put(tieto.getOid(), tieto);
        }

        Set<String> givenOids = givenData.keySet();
        List<String> oids = new ArrayList<>(givenOids);

        LOG.debug("bulkUpdatePvm(): haetaan oideilla:" + oids);
        List<Organisaatio> organisaatios = this.organisaatioDAO.findByOidList(oids, oids.size());

        if (organisaatios.isEmpty()) {
            LOG.debug("bulkUpdatePvm(): organisaatiolista tyhjä");
            return edited; // tässä vaiheessa tyhjä lista.
        }
        LOG.debug("bulkUpdatePvm(): organisaatiolista:" + organisaatios);

        // näiden oidien vanhemmuussuhteet on jo löydetty
        Set<String> processed = new HashSet<>(tiedot.size());
        List<Organisaatio> roots = new ArrayList<>(tiedot.size());

        // etsitään juuriorganisaatiot, eli ne, joiden vanhempaa ei löydy annetuista oideista
        for (Organisaatio o : organisaatios) {
            LOG.debug("bulkUpdatePvm(): käsitellään organisaatio:" + o + ",oid:" + o.getOid());
            organisaatioMap.put(o.getOid(), o);
            while (!processed.contains(o.getOid())) {
                processed.add(o.getOid());

                // jos vanhempaa ei löyty annetusta oidlistasta, tämä on juuriorganisaatio
                if (!givenOids.contains(o.getParent().getOid())) {
                    roots.add(o);
                    break;
                }
                o = o.getParent();
            }
        }
        LOG.debug("bulkUpdatePvm(): processed:" + processed);

        String virheViesti = "";
        // tarkistetaan ettei minkään juuriorganisaatio alta löydy päivämääriä jotka rikkovat rajat
        for (Organisaatio o: roots) {
            virheViesti = checker.checkPvmConstraints(o, null, null, givenData);
            if (!virheViesti.equals("")) {
                LOG.error(String.format("bulkUpdatePvm() error: %s", virheViesti));
                throw new OrganisaatioDateException();
            }
        }
        for(String oid: organisaatioMap.keySet()) {
            OrganisaatioMuokkausTiedotDTO tieto = givenData.get(oid);
            Organisaatio org = organisaatioMap.get(oid);

            if (tieto != null) {
                LOG.debug(String.format("bulkUpdatePvm(): testataan onko Organisaatiolla (oid %s) koulutuksia loppupäivämäärän %s jälkeen", org.getOid(), tieto.getLoppuPvm()));
                if ((tieto.getLoppuPvm() != null) && (organisaatioTarjonta.alkaviaKoulutuksia(oid, tieto.getLoppuPvm()))) {
                    String virhe = String.format("Organisaatiolla (oid %s) koulutuksia jotka alkavat lakkautuspäivämäärän (%s) jälkeen", oid, tieto.getLoppuPvm());
                    LOG.error(String.format(virhe));
                    throw new AliorganisaatioLakkautusKoulutuksiaException();
                }
            }
        }

        List<Organisaatio> indeksoitavat = new ArrayList<>(givenData.size());
        for(String oid: organisaatioMap.keySet()) {
            OrganisaatioMuokkausTiedotDTO tieto = givenData.get(oid);
            Organisaatio org = organisaatioMap.get(oid);

            if (tieto != null) {
                LOG.debug(String.format("bulkUpdatePvm(): ennen päivitystä: oid %s, version %s", org.getOid(), org.getVersion()));
                org.setAlkuPvm(tieto.getAlkuPvm());
                org.setLakkautusPvm(tieto.getLoppuPvm());
                try {
                    organisaatioDAO.update(org);
                    organisaatioKoodisto.paivitaKoodisto(org, false);
                } catch (OptimisticLockException ole) {
                    LOG.error(String.format("Organisaation (oid %s) muokkaus epäonnistui versionumeron muuttumisen takia", org.getOid()));
                    throw new AliorganisaatioModifiedException(ole);
                }

                LOG.debug(String.format("bulkUpdatePvm(): päivityksen jälkeen: oid %s, version %s", org.getOid(), org.getVersion()));

                OrganisaatioMuokkausTulosDTO tulos = new OrganisaatioMuokkausTulosDTO();
                tulos.setAlkuPvm(org.getAlkuPvm());
                tulos.setLoppuPvm(org.getLakkautusPvm());
                tulos.setOid(org.getOid());
                tulos.setVersion(org.getVersion());

                edited.lisaaTulos(tulos);

                indeksoitavat.add(org);
            }
        }
        solrIndexer.index(indeksoitavat);

        return edited;
    }

    @Override
    public List<Organisaatio> processNewOrganisaatioSuhdeChanges() {
        List<Organisaatio> results = new ArrayList<>();
        List<OrganisaatioSuhde> suhdeList = organisaatioSuhdeDAO.findForDay(new Date());
        Set<Organisaatio> affectedParents = new HashSet<>();
        for (OrganisaatioSuhde os : suhdeList) {
            LOG.info("Processing {}", os);
            affectedParents.add(os.getParent());

            Organisaatio child = os.getChild();
            // Find previous parent organisation.
            List<OrganisaatioSuhde> parentRelations = child.getParentSuhteet(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
            Collections.reverse(parentRelations);
            if (parentRelations.size() > 1) {
                Organisaatio oldParent = parentRelations.get(1).getParent();
                affectedParents.add(oldParent);
            }
            setParentPath(child, os.getParent().getOid());
            organisaatioDAO.update(child);
            solrIndexer.index(child);

            updateChildrenRecursive(child);

            results.add(child);
        }

        // Update index for affected parent organisations.
        solrIndexer.index(new ArrayList<>(affectedParents));

        return results;
    }

    @Override
    public void mergeOrganisaatio(Organisaatio organisaatio, Organisaatio newParent, Date date) {
        // Organisaatiota ei saa liittää itseensä
        if (organisaatio.getOid().equals(newParent.getOid())) {
            throw new OrganisaatioMoveException("organisation.move.merge.self");
        }

        // Organisaatiota ei saa yhdistää eri organisaatiotasolla olevaan organisaatioon
        // Organisaatioista on löydyttävä ainakin 1 yhteinen tyyppi
        if (!CollectionUtils.containsAny(organisaatio.getTyypit(), newParent.getTyypit())) {
            throw new OrganisaatioMoveException("organisation.move.merge.level");
        }

        // Organisaatiota ei saa yhdistää lakkautettuun tai poistettuun organisaatioon
        if (newParent.isOrganisaatioPoistettu() != false || OrganisaatioUtil.isPassive(newParent)) {
            throw new OrganisaatioMoveException("organisation.move.merge.parent.invalid");
        }

        // Lakkautetaan yhdistyvä organisaatio
        Calendar previousDay = Calendar.getInstance();
        previousDay.setTime(date);
        previousDay.add(Calendar.DAY_OF_MONTH, -1);
        organisaatio.setLakkautusPvm(previousDay.getTime());
        organisaatioDAO.update(organisaatio);
        solrIndexer.index(organisaatio);

        // Lisätään uusi organisaatioiden liitos
        organisaatioSuhdeDAO.addLiitos(organisaatio, newParent, date);

        // Siirretään kaikki aktiiviset aliorganisaatiot uuden parentin alle
        final List<OrganisaatioSuhde> suhteet =
                organisaatioSuhdeDAO.findChildrenTo(organisaatio.getId(), date);
        for (OrganisaatioSuhde suhde : suhteet) {
            Organisaatio child = suhde.getChild();
            if (OrganisaatioUtil.isPassive(child) == false) {
                changeOrganisaatioParent(child, newParent, date);
            }
        }

        // Päivitetään tiedot koodistoon.
        organisaatioKoodisto.paivitaKoodisto(organisaatio, true);
    }

    // Updates nimi and osoitetieto for all Koulutustoimija, Muu_organisaatio and Tyoelamajarjesto organisations
    @Override
    public List<Organisaatio> updateYTJData(final boolean forceUpdate) {
        // Create y-tunnus list of updateable arganisations
        List<String> oidList = new ArrayList<>();
        List<String> ytunnusList = new ArrayList<>();
        List<Organisaatio> organisaatioList;
        List<Organisaatio> updateOrganisaatioList = new ArrayList<>();
        int searchLimit = 10000;
        // Search the organisations using the DAO since it provides osoites.
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        if(oidList.isEmpty()) {
            LOG.debug("oidList is empty, no organisations updated from YTJ!");
            return updateOrganisaatioList;
        }
        organisaatioList = organisaatioDAO.findByOidList(oidList, searchLimit);
        // Fill the Y-tunnus list and parse off organisaatios that are lakkautettu
        for(Organisaatio organisaatio : organisaatioList) {
            if(organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.AKTIIVINEN
                    || organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.SUUNNITELTU) {
                ytunnusList.add(organisaatio.getYtunnus());
            }
        }

        // Fetch data from ytj for these organisations
        List<YTJDTO> ytjdtoList = ytjResource.findByYTunnusBatch(ytunnusList);

        Map<String,Organisaatio> organisaatioMap = new HashMap<String,Organisaatio>();
        for (Organisaatio o : organisaatioList) {
            organisaatioMap.put(o.getYtunnus().trim(),o);
        }
        // Check which organisations need to be updated. YtjPaivitysPvm is the date when info is fetched from YTJ.
        for (YTJDTO ytjdto : ytjdtoList) {
            Organisaatio organisaatio = organisaatioMap.get(ytjdto.getYtunnus().trim());

            if (organisaatio != null) {
                Boolean updateNimi = false;
                Boolean updateOsoite = false;
                Boolean updatePuhelin = false;
                Boolean updateWww = false;
                // Update nimi
                // There should always exist at least one nimi.
                if (organisaatio.getNimi() == null) {
                    LOG.warn("Organisation does not have a name. Invalid organisation. Not updating.");
                    // TODO we don't need to check this if we do the name stuff differently and just create and add the new name...
                    continue;
                }
                else if ((organisaatio.getNimi() != null && ytjdto.getNimi() != null
                        && !ytjdto.getNimi().equals(organisaatio.getNimi().getString("fi")))
                        || (ytjdto.getSvNimi() != null && organisaatio.getNimi() != null
                        && !ytjdto.getSvNimi().equals(organisaatio.getNimi().getString("sv")))
                        || forceUpdate) {
                    updateNamesFromYTJ(ytjdto, organisaatio);
                    updateNimi = true;
                }

                // Update Osoite
                // Find osoite with right language (finnish or swedish)
                Osoite osoite = findOsoiteByLangAndTypeFromYhteystiedot(ytjdto, organisaatio);
                // No matching kieli found from organisation so we will create an empty one to be fetched from YTJ.
                // (organisation language could be eg. fi/sv (dual) or en which are not in YTJ)
                if (osoite == null) {
                    try{
                        osoite = addOsoiteForOrgFromYTJData(ytjdto, organisaatio);
                    } catch (ExceptionMessage e) {
                        // handle properly if adding failed
                        LOG.error("Could not generate oid, skipping organisation", e);
                        continue;
                    }
                }
                updateOsoite = updateAddressDataFromYTJ(ytjdto, osoite, forceUpdate);

                // Update puhelin
                if(ytjdto.getPuhelin() != null) {
                    try {
                        updatePuhelin = updatePuhelinFromYTJtoOrganisaatio(forceUpdate, ytjdto, organisaatio);
                    } catch (ExceptionMessage e) {
                        LOG.error("Could not generate oid for Puhelinnumero, skipping organisation", e);
                        continue;
                    }
                }

                // Update www
                if(ytjdto.getWww() != null) {
                    try {
                        updateWww = updateWwwFromYTJToOrganisation(forceUpdate, ytjdto, organisaatio);
                    } catch (ExceptionMessage e) {
                        LOG.error("Could not generate oid for Www, skipping organisation", e);
                        continue;
                    }

                }

                if (updateNimi || updateOsoite || updatePuhelin || updateWww) {
                    // add new kieli to the organisation if there isn't one matching the YTJ kieli
                    updateLangFromYTJ(ytjdto, organisaatio);
                    updateOrganisaatioList.add(organisaatio);
                }
            }
        }

        // Update these organisations
        for(Organisaatio organisaatio : updateOrganisaatioList) {
            try {
                checker.checkNimihistoriaAlkupvm(organisaatio.getNimet());
                organisaatioDAO.update(organisaatio);
                // update koodisto (When name has changed) TODO: call only when name actually changes.
                // Update only nimi if changed. organisaatio.paivityspvm should not have be changed here.
                organisaatioKoodisto.paivitaKoodisto(organisaatio, false);
            } catch(OrganisaatioNameHistoryNotValidException onhnve) {
                LOG.error("Organisation name history alkupvm invalid with organisation " + organisaatio.getOid(), onhnve);
            } catch(OrganisaatioKoodistoException e) {
                LOG.error("Could not update name to koodisto with organisation " + organisaatio.getOid(), e);
            } catch (OptimisticLockException ole) {
                LOG.error("Java persistance exception with organisation " + organisaatio.getOid(), ole.getMessage());
            } catch (RuntimeException re) {
                LOG.error("Could not update organisation " + organisaatio.getOid(), re);
            }
        }
        // Index the updated resources.
        solrIndexer.index(updateOrganisaatioList);

        return updateOrganisaatioList;
    }

    private boolean updateWwwFromYTJToOrganisation(boolean forceUpdate, YTJDTO ytjdto, Organisaatio organisaatio)
            throws ExceptionMessage {
        ytjdto.setWww(fixHttpPrefix(ytjdto.getWww()));
        Www www = null;
        boolean update = false;
        for(Yhteystieto yhteystieto : organisaatio.getYhteystiedot()) {
            if(yhteystieto instanceof Www) {
                www = (Www)yhteystieto;
                break;
            }
        }
        // Create new www if one does not exist
        if(www == null) {
            www = new Www(oidService.newOid(NodeClassCode.TEKN_5));
            www.setOrganisaatio(organisaatio);
            if (ytjdto.getYrityksenKieli() != null
                    && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                www.setKieli(KIELI_KOODI_SV);
            } else {
                www.setKieli(KIELI_KOODI_FI);
            }

            organisaatio.addYhteystieto(www);
            update = true;
        }
        // Update www from YTJ if it missmatches the current one.
        if((!ytjdto.getWww().equals(www.getWwwOsoite()))
                || forceUpdate) {
            www.setWwwOsoite(ytjdto.getWww());
            update = true;
        }
        return update;
    }

    private boolean updatePuhelinFromYTJtoOrganisaatio(boolean forceUpdate, YTJDTO ytjdto, Organisaatio organisaatio)
            throws ExceptionMessage {
        boolean update = false;
        // Parse extra stuff off.
        ytjdto.setPuhelin(ytjdto.getPuhelin().split(",|; *")[0]);
        // Create new puhelinnumero if one does not exist
        if(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) == null) {
            Puhelinnumero puhelinnumero =
                    new Puhelinnumero("   ", Puhelinnumero.TYYPPI_PUHELIN, oidService.newOid(NodeClassCode.TEKN_5));
            puhelinnumero.setOrganisaatio(organisaatio);
            if (ytjdto.getYrityksenKieli() != null
                    && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                puhelinnumero.setKieli(KIELI_KOODI_SV);
            } else {
                puhelinnumero.setKieli(KIELI_KOODI_FI);
            }
            organisaatio.addYhteystieto(puhelinnumero);
            update = true;
        }
        // Update puhelinnumero from YTJ if it missmatches the current one.
        if((organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) != null
                && (!ytjdto.getPuhelin().equals(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero()))
                || forceUpdate)) {
            organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).setPuhelinnumero(ytjdto.getPuhelin());
            update = true;
        }
        return update;
    }

    private String fixHttpPrefix(String www) {
        if(www != null && !www.matches("^(https?:\\/\\/).*$")) {
            www = "http://" + www;
        }
        return www;
    }

    private void updateLangFromYTJ(YTJDTO ytjdto, Organisaatio organisaatio) {
        Boolean kieliExists = false;
        for (String kieli : organisaatio.getKielet()) {
            if (kieli.trim().equals(ORG_KIELI_KOODI_FI)
                    && ytjdto.getYrityksenKieli().trim().equals("Suomi")) {
                kieliExists = true;
                break;
            }
            if (kieli.trim().equals(ORG_KIELI_KOODI_SV)
                    && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                kieliExists = true;
                break;
            }
        }
        if (!kieliExists) {
            String newKieli = "";
            List<String> newKieliList = new ArrayList<>();
            if (ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                newKieli = ORG_KIELI_KOODI_SV;
            } else {
                newKieli = ORG_KIELI_KOODI_FI;
            }
            for (String kieli : organisaatio.getKielet()) {
                newKieliList.add(kieli);
            }
            newKieliList.add(newKieli);
            organisaatio.setKielet(newKieliList);
        }
    }

    private Boolean updateAddressDataFromYTJ(YTJDTO ytjdto, Osoite osoite, final boolean forceUpdate) {
        Boolean update = false;
        if (ytjdto.getPostiOsoite() != null && ytjdto.getPostiOsoite().getPostinumero() != null
                && (!("posti_" + ytjdto.getPostiOsoite().getPostinumero().trim()).equals(osoite.getPostinumero())
                || forceUpdate)) {
            osoite.setPostinumero("posti_" + ytjdto.getPostiOsoite().getPostinumero().trim());
            osoite.setYtjPaivitysPvm(new Date());
            update = true;
        }
        if (ytjdto.getPostiOsoite() != null && ytjdto.getPostiOsoite().getKatu() != null
                && (!ytjdto.getPostiOsoite().getKatu().trim().equals(osoite.getOsoite())
                || forceUpdate)) {
            osoite.setOsoite(ytjdto.getPostiOsoite().getKatu().trim());
            osoite.setYtjPaivitysPvm(new Date());
            update = true;
        }
        if (ytjdto.getPostiOsoite() != null && ytjdto.getPostiOsoite().getToimipaikka() != null
                && (!ytjdto.getPostiOsoite().getToimipaikka().trim().equals(osoite.getPostitoimipaikka())
                || forceUpdate)) {
            osoite.setPostitoimipaikka(ytjdto.getPostiOsoite().getToimipaikka().trim());
            osoite.setYtjPaivitysPvm(new Date());
            update = true;
        }
        return update;
    }

    private Osoite addOsoiteForOrgFromYTJData(YTJDTO ytjdto, Organisaatio organisaatio) throws ExceptionMessage {
        Osoite osoite;
        osoite = new Osoite();
        osoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
        osoite.setOrganisaatio(organisaatio);
        try {
            osoite.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
        } catch (ExceptionMessage e) {
            LOG.error("Could not generate oid, not updating this organisation", e);
            throw e;
        }
        if (ytjdto.getYrityksenKieli() != null
                && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
            osoite.setKieli(KIELI_KOODI_SV);
        } else {
            osoite.setKieli(KIELI_KOODI_FI);
        }
        organisaatio.addYhteystieto(osoite);
        return osoite;
    }

    private Osoite findOsoiteByLangAndTypeFromYhteystiedot(YTJDTO ytjdto, Organisaatio organisaatio) {
        Osoite osoite = null;
        for (Yhteystieto yhteystieto : organisaatio.getYhteystiedot()) {
            if (yhteystieto instanceof Osoite && yhteystieto.getKieli().trim().equals(KIELI_KOODI_SV)
                    && ytjdto.getYrityksenKieli() != null
                    && ytjdto.getYrityksenKieli().equals(YtjDtoMapperHelper.KIELI_SV)) {
                if(((Osoite) yhteystieto).getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE)) {
                    osoite = (Osoite) yhteystieto;
                    break;
                }
            }
            if (yhteystieto instanceof Osoite && yhteystieto.getKieli().trim().equals(KIELI_KOODI_FI)) {
                if(((Osoite) yhteystieto).getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE)) {
                    osoite = (Osoite) yhteystieto;
                    break;
                }
            }
        }
        return osoite;
    }

    private void updateNamesFromYTJ(YTJDTO ytjdto, Organisaatio organisaatio) {
        if (organisaatio.getNimi().getString("fi") != null || organisaatio.getNimi().getString("sv") != null) {
            // TODO I still don't like this solution.
            // save copy of old nimi to organisaatio nimet as history and modify the old one.
            for (final OrganisaatioNimi orgNimi : organisaatio.getNimet()) {
                // Update nimet (history) with a copy of the old current nimi (orgNimi)
                if (orgNimi.getNimi() == organisaatio.getNimi()) {
                    // Check equality in case of forceUpdate to prevent spam in name history.
                    if((ytjdto.getNimi() != null && orgNimi.getNimi().getString("fi") != null
                            && !ytjdto.getNimi().equals(orgNimi.getNimi().getString("fi")))
                            ||
                            (ytjdto.getSvNimi() != null && orgNimi.getNimi().getString("sv") != null)
                            && !ytjdto.getSvNimi().equals(orgNimi.getNimi().getString("sv"))) {
                        // Create new entry to nimihistoria
                        MonikielinenTeksti newNimi = new MonikielinenTeksti();
                        OrganisaatioNimi newOrgNimi = new OrganisaatioNimi();
                        // Add only the language to be updated to the history entry
                        if(ytjdto.getSvNimi() != null) {
                            newNimi.setValues(new HashMap<String, String>() {{put("sv", orgNimi.getNimi().getValues().get("sv"));}});
                        }
                        else {
                            newNimi.setValues(new HashMap<String, String>() {{put("fi", orgNimi.getNimi().getValues().get("fi"));}});
                        }
                        newOrgNimi.setNimi(newNimi);
                        newOrgNimi.setPaivittaja(orgNimi.getPaivittaja());
                        newOrgNimi.setOrganisaatio(orgNimi.getOrganisaatio());
                        newOrgNimi.setAlkuPvm(orgNimi.getAlkuPvm());
                        organisaatio.addNimi(newOrgNimi);

                    }
                    // When updating nimi always update alkupvm from YTJ as toiminimen alkupvm.
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        orgNimi.setAlkuPvm(format.parse(ytjdto.getAloitusPvm()));
                    }
                    catch(ParseException | NullPointerException e) {
                        LOG.error("Could not parse YTJ date. Using the old date.", e);
//                        orgNimi.setAlkuPvm(null);
                    }
                    break;
                }
            }
        }
        organisaatio.setYtjPaivitysPvm(new Date());
        // Update the old nimi which already is already referred from organisaatio.metadata,
        // organisaatio.nimet.organisaationimi and organisaatio.
        if (ytjdto.getNimi() != null) {
            organisaatio.getNimi().getValues().put("fi", ytjdto.getNimi());
            organisaatio.setNimihaku(ytjdto.getNimi());
        }
        if (ytjdto.getSvNimi() != null) {
            organisaatio.getNimi().getValues().put("sv", ytjdto.getSvNimi());
            organisaatio.setNimihaku(ytjdto.getSvNimi());
        }
    }

    @Override
    public void changeOrganisaatioParent(Organisaatio organisaatio, Organisaatio newParent, Date date) {
        // Organisaatiota ei saa siirtää nykyisen parentin alle
        if (organisaatio.getParent().getOid().equals(newParent.getOid())) {
            throw new OrganisaatioMoveException("organisation.move.parent.invalid");
        }

        // Organisaatiota ei saa siirtää väärällä hierarkiatasolla olevaan organisaatioon
        checker.checkParentChildHierarchy(organisaatio, newParent);

        // Organisaatiota ei saa siirtää lakkautettuun tai poistettuun organisaatioon
        if (newParent.isOrganisaatioPoistettu() != false || OrganisaatioUtil.isPassive(newParent)) {
            throw new OrganisaatioMoveException("organisation.move.parent.invalid");
        }

        OrganisaatioSuhde currentParentRelationship = organisaatioSuhdeDAO.findParentTo(organisaatio.getId(), null);
        currentParentRelationship.setLoppuPvm(date);
        organisaatioSuhdeDAO.update(currentParentRelationship);

        // Luodaan uusi suhde
        // HUOM! Tässä pitää laittaa organisaatiosuhde "organisaation kautta --> näin parent laskenta pysyy mukana"
        OrganisaatioSuhde parentRelation = new OrganisaatioSuhde();
        parentRelation.setSuhdeTyyppi(OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA);
        parentRelation.setAlkuPvm(date);
        parentRelation.setLoppuPvm(null);
        parentRelation.setChild(organisaatio);
        parentRelation.setParent(newParent);

        // Asetetaan uusi parentsuhde
        organisaatio.getParentSuhteet().add(parentRelation);

        // Organisaatiolla on uusi parent, toimipisteen tapauksessa ollaan uuden oppilaitoksen alla.
        // Lasketaan toimipisteelle uusi opetuspisteen järjestysnumero ja asetetaan toimipistekoodi.
        // Mahdollinen vanha toimipistekoodi pitää lakkauttaa ja luoda uusi koodiston koodi.
        if (OrganisaatioUtil.isToimipiste(organisaatio)) {
            // Vanha toimipistekoodi tarvitaan pitää lakkauttaa
            String oldToimipistekoodi = organisaatio.getToimipisteKoodi();

            String opJarjNro = generateOpetuspisteenJarjNro(organisaatio);
            organisaatio.setOpetuspisteenJarjNro(opJarjNro);
            organisaatio.setToimipisteKoodi(calculateToimipisteKoodi(organisaatio));
            parentRelation.setOpetuspisteenJarjNro(opJarjNro);

            Calendar previousDay = Calendar.getInstance();
            previousDay.setTime(date);
            previousDay.add(Calendar.DAY_OF_MONTH, -1);

            // Lakkautetaan vanha opetuspistekoodi (opetuspiste pistetään lakkaamaan päivää ennen)
            organisaatioKoodisto.lakkautaKoodi(OrganisaatioKoodisto.KoodistoUri.TOIMIPISTE.uri(), oldToimipistekoodi, previousDay.getTime(), true);

            // Päivitetään uusi opetuspistekoodi koodistoon.
            organisaatioKoodisto.paivitaKoodisto(organisaatio, true);
        }

        // Päivitetään suhteet ja indeksointi, jos uusi parent on jo voimassa (date == tänään / aiemmin)
        Date today = new Date();
        if (date.before(today) || DateUtils.isSameDay(date, today)) {
            setParentPath(organisaatio, newParent.getOid());
            organisaatioDAO.update(organisaatio);
            solrIndexer.index(organisaatio);

            updateChildrenRecursive(organisaatio);
        }
    }

    private Organisaatio updateCurrentNimiToOrganisaatio(Organisaatio organisaatio) {
        // Haetaan organisaation current nimi
        OrganisaatioNimi nimiEntity = this.organisaatioNimiDAO.findCurrentNimi(organisaatio);

        if (nimiEntity == null) {
            throw new OrganisaatioNimiNotFoundException(organisaatio.getOid());
        }

        // Päivitetään organisaation nimi
        organisaatio.setNimi(nimiEntity.getNimi());

        LOG.info("updating " + organisaatio);
        try {
            // Päivitetään nimi
            organisaatioDAO.update(organisaatio);
        } catch (OptimisticLockException ole) {
            throw new OrganisaatioModifiedException(ole);
        }

        // Palautetaan päivitetty organisaatio
        return organisaatioDAO.read(organisaatio.getId());
    }

    @Override
    public void updateCurrentOrganisaatioNimet() {
        // Haetaan organisaatiot, joiden nimi ei ole nimihistorian current nimi
        List<Organisaatio> organisaatiot = this.organisaatioNimiDAO.findNimiNotCurrentOrganisaatiot();

        if (organisaatiot.isEmpty()) {
            LOG.info("Orgnisaatioiden nimet kunnossa");
        }

        for (Organisaatio organisaatio : organisaatiot) {
            Map<String, String> oldName;
            oldName = new HashMap<>(organisaatio.getNimi().getValues());

            LOG.info("Orgnisaation nimen update tarve: " + organisaatio);

            // Päiviteään organisaatiolle nimihistorian current nimi
            organisaatio = this.updateCurrentNimiToOrganisaatio(organisaatio);

            // Indeksoidaan organisaatio solriin uudella nimellä
            solrIndexer.index(Lists.newArrayList(organisaatio));

            // Tarkistetaan ja päivitetään oppilaitoksen alla olevien opetuspisteiden nimet
            if (organisaatioIsOfType(organisaatio, OrganisaatioTyyppi.OPPILAITOS)) {
                // Ei päivitetä organisaation päivittäjää nimenmuutoksen yhteydessä
                updateOrganisaatioNameHierarchy(organisaatio, oldName, false);
            }

            // Päivitetään tiedot koodistoon.
            organisaatioKoodisto.paivitaKoodisto(organisaatio, true);
        }
    }
}
