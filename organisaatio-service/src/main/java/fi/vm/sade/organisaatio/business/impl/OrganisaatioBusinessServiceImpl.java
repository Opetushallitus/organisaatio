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
import fi.vm.sade.organisaatio.business.exception.LearningInstitutionExistsException;
import fi.vm.sade.organisaatio.business.exception.NoVersionInKoodistoUriException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioExistsException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioHierarchyException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioLakkautusKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioModifiedException;
import fi.vm.sade.organisaatio.business.exception.YtunnusException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoArvoDAO;
import fi.vm.sade.organisaatio.dao.YhteystietoElementtiDAO;
import fi.vm.sade.organisaatio.dao.YhteystietojenTyyppiDAO;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.model.YhteystietoArvo;
import fi.vm.sade.organisaatio.model.YhteystietoElementti;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.model.lop.NamedMonikielinenTeksti;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.OrganisationDateValidator;
import fi.vm.sade.organisaatio.service.OrganisationHierarchyValidator;
import fi.vm.sade.organisaatio.service.util.OrganisaatioUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.persistence.OptimisticLockException;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private IndexerResource solrIndexer;

    @Autowired
    private OIDService oidService;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioKoulutukset organisaatioKoulutukset;

    @Autowired
    private OrganisaatioKoodisto organisaatioKoodisto;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    private static final String parentSeparator = "|";
    private static final String parentSplitter = "\\|";
    private static final String uriWithVersionRegExp = "^.*#[0-9]+$";

    @Override
    @Transactional(readOnly = true)
    public List<Organisaatio> findBySearchCriteria(
            List<String> kieliList,
            List<String> kuntaList,
            List<String> oppilaitostyyppiList,
            List<String> vuosiluokkaList,
            List<String> ytunnusList,
            List<String> oidList,
            int limit) {

        return organisaatioDAO.findBySearchCriteria(kieliList, kuntaList, oppilaitostyyppiList, vuosiluokkaList, ytunnusList, oidList, limit);
    }

    private void mergeAuxData(Organisaatio entity, Organisaatio orgEntity) {
        try {
            if (orgEntity.getNimi() != null) {
                entity.getNimi().setId(orgEntity.getNimi().getId());
                entity.getNimi().setVersion(orgEntity.getNimi().getVersion());
            }
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
    public Organisaatio save(OrganisaatioRDTO model, boolean updating, boolean skipParentDateValidation) throws ValidationException {
        // Tarkistetaan OID
        if (model.getOid() == null && updating) {
            throw new ValidationException("Oid cannot be null");//trying to update organisaatio that doesn't exist (is is null)");
        } else if (!updating) {
            if ((model.getOid() != null) && (organisaatioDAO.findByOid(model.getOid()) != null)) {
                throw new OrganisaatioExistsException(model.getOid());
            }

            if (model.getOppilaitosKoodi() != null && model.getOppilaitosKoodi().length() > 0) {
                if (checkLearningInstitutionCodeIsUniqueAndNotUsed(model)) {
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
        if (OrganisaatioUtil.isRyhma(model) && model.getParentOid().equalsIgnoreCase(rootOrganisaatioOid) == false ) {
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
        checkVersionInKoodistoUris(model);

        // Luodaan tallennettava entity objekti
        Organisaatio entity = conversionService.convert(model, Organisaatio.class); //this entity is populated with new data

        // Asetetaan parent path
        createParentPath(entity, model.getParentOid());

        // Asetetaan päivittäjä ja päivityksen aikaleima
        try {
            entity.setPaivittaja(getCurrentUser());
            entity.setPaivitysPvm(new Date());
        } catch (Throwable t) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, t.getMessage(), "error.setting.updater");
        }

        // Päivitystapauksessa pitaa asetta id:t, ettei luoda uusia rivejä
        if (updating) {
            Organisaatio orgEntity = this.organisaatioDAO.findByOid(model.getOid());
            mergeAuxData(entity, orgEntity);
            entity.setId(orgEntity.getId());
            entity.setOpetuspisteenJarjNro(orgEntity.getOpetuspisteenJarjNro());

            // Tarkistetaan organisaatiohierarkia jos hierarkia muuttunut (onko parent muuttunut)
            if (model.getParentOid().equals(orgEntity.getParent().getOid()) == false) {
                LOG.info("Hierarkia muuttunut, tarkastetaan hierarkia.");
                checkOrganisaatioHierarchy(entity, model.getParentOid());
            }

            // Tarkistetaan organisaatiohierarkia jos organisaatiotyypit muutuneet
            if (!entity.getTyypit().containsAll(orgEntity.getTyypit()) ||
                    !orgEntity.getTyypit().containsAll(entity.getTyypit())) {
                LOG.info("Organisaation tyypit muuttuneet, tarkastetaan hierarkia.");
                checkOrganisaatioHierarchy(entity, model.getParentOid());
            }

            // Tarkistetaan ettei lakkautuspäivämäärän jälkeen ole alkavia koulutuksia
            if (OrganisaatioUtil.isSameDay(entity.getLakkautusPvm(), orgEntity.getLakkautusPvm()) == false) {
                LOG.info("Lakkautuspäivämäärä muuttunut, tarkastetaan alkavat koulutukset.");
                checkLakkautusAlkavatKoulutukset(entity);
            }
        }
        else {
            // Tarkistetaan organisaatio hierarkia
            checkOrganisaatioHierarchy(entity, model.getParentOid());
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
            opJarjNro = generateOpetuspisteenJarjNro(entity, model);
        } else {
            opJarjNro = entity.getOpetuspisteenJarjNro();
        }

        // If inserting, check if ytunnus allready exists in the database
        if (!updating && entity.getYtunnus() != null) {
            checkYtunnusIsUniqueAndNotUsed(entity.getYtunnus());
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

        for (Yhteystieto yhtTieto : entity.getYhteystiedot()) {
            yhtTieto.setOrganisaatio(entity);
        }

        // Asetetaan tyypit "organisaatio" taulun kenttään
        List<String> orgTypes = new ArrayList<String>();
        String tyypitStr = "";
        for (String curTyyppi : model.getTyypit()) {
            orgTypes.add(curTyyppi);
            tyypitStr += curTyyppi + "|";
        }
        entity.setOrganisaatiotyypitStr(tyypitStr);


        // Generate natural key, OVT-4954
        // "Jos kyseessä on koulutustoimija pitäisi palauttaa y-tunnus."
        // "Jos oppilaitos, palautetaan oppilaitosnumero."
        // "Jos toimipiste, palautetaan oppilaitosnro+toimipisteenjärjestysnumero(konkatenoituna)sekä yhkoulukoodi."
        entity.setToimipisteKoodi(calculateAndUpdateToimipisteKoodi2(entity));

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
        if (OrganisaatioUtil.isRyhma(entity) == false) {
            solrIndexer.index(Lists.newArrayList(entity));
        }

        // Päivitä tiedot koodistoon
        updateKoodisto(entity);

        return entity;
    }

    private Organisaatio saveParentSuhde(Organisaatio child, Organisaatio parent, String opJarjNro) {
        OrganisaatioSuhde curSuhde = organisaatioSuhdeDAO.findParentTo(child.getId(), null);
        if (parent != null && (curSuhde == null || curSuhde.getParent().getId() != parent.getId())) {
            curSuhde = organisaatioSuhdeDAO.addChild(parent.getId(), child.getId(), Calendar.getInstance().getTime(), opJarjNro);
        }
        child.setParentSuhteet(organisaatioSuhdeDAO.findBy("child", child));
        return this.organisaatioDAO.findByOid(child.getOid());
    }

        private List<YhteystietoArvo> mergeYhteystietoArvos(Organisaatio org, List<YhteystietoArvo> nys,
            boolean updating) {

        Map<String, YhteystietoArvo> ov = new HashMap<String, YhteystietoArvo>();

        for (YhteystietoArvo ya : yhteystietoArvoDAO.findByOrganisaatio(org)) {
            if (!isAllowed(org, ya.getKentta().getYhteystietojenTyyppi())) {
                yhteystietoArvoDAO.remove(ya);
            } else {
                ov.put(ya.getKentta().getOid() + ya.getKieli(), ya);
            }
        }

        List<YhteystietoArvo> ret = new ArrayList<YhteystietoArvo>();

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


    /**
     * Simple recursive operuspiste / toimipiste koodi calculation.
     *
     * Search "up" for OPPILAITOS and return it's OppilaitosKoodi and then
     * append OPETUSPISTE order number(s).
     *
     * @param s
     * @return
     */
    private String calculateAndUpdateToimipisteKoodi2(Organisaatio s) {
        LOG.debug("calculateAndUpdateToimipisteKoodi2(org={})", s);

        if (s == null) {
            LOG.debug("  org  == null, return ''");
            return "";
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.OPPILAITOS)) {
            LOG.debug("  org  == OPPILAITOS, return oppilaitoskoodi: '{}'", s.getOppilaitosKoodi());
            return s.getOppilaitosKoodi();
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.TOIMIPISTE)) {
            LOG.debug("  org  == TOIMIPISTE, return parent opk/olk code AND this ones order number: '{}'", s.getOpetuspisteenJarjNro());
            String onum = isEmpty(s.getOpetuspisteenJarjNro()) ? "01" : s.getOpetuspisteenJarjNro();
            Organisaatio parent = null;
            if (s.getId() != null) {
                parent = (s.getParent() != null) ? s.getParent() : this.organisaatioSuhdeDAO.findParentTo(s.getId(), new Date()).getParent();
            } else {
                String[] parentOids = s.getParentOidPath().split("\\|");
                parent = (s.getParent() != null) ? s.getParent() : organisaatioDAO.findByOid(parentOids[parentOids.length - 1]);
            }
            return calculateAndUpdateToimipisteKoodi2(parent) + onum;
        }

        LOG.debug("calculateAndUpdateToimipisteKoodi2 == TYPE unknown?: types='{}'", s.getTyypit());

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

    /**
     * Check that given code has not been used.
     *
     * @param org
     * @return
     */
    private boolean checkLearningInstitutionCodeIsUniqueAndNotUsed(OrganisaatioRDTO org) {
        List<Organisaatio> orgs = organisaatioDAO.findBy("oppilaitosKoodi", org.getOppilaitosKoodi().trim());
        if (orgs != null && orgs.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Check that given toimipistekoodi code has not been used.
     *
     * @param org
     * @return
     */
    private boolean checkToimipistekoodiIsUniqueAndNotUsed(String toimipistekoodi) {
        List<Organisaatio> orgs = organisaatioDAO.findBy("toimipisteKoodi", toimipistekoodi.trim());
        if (orgs != null && orgs.size() > 0) {
            // toimipistekoodi on jo olemassa
            LOG.debug("Toimipistekoodi already exists: " + toimipistekoodi);
            return false;
        }

        return true;
    }

    private void generateOids(Organisaatio organisaatio) throws ExceptionMessage {
        if (organisaatio.getOid() == null) {
            if (OrganisaatioUtil.isRyhma(organisaatio)) {
                /// @TODO: Sitten kun Oid palveluss on ryhmä mukana --> korjaa
                //  organisaatio.setOid(oidService.newOid(NodeClassCode.RYHMA));

                organisaatio.setOid(oidService.newOidByClassValue("28"));
            }
            else {
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

    /*
    * Generating the opetuspiteenJarjNro for an opetuspiste.
    * The opetuspiteenJarjNro is the count of the cescendants of the parent oppilaitos + 1.
    */
    private String generateOpetuspisteenJarjNro(Organisaatio entity,
            OrganisaatioRDTO model) {
        //Opetuspisteen jarjestysnumero is only generated to opetuspiste which is not also an oppilaitos
        if (model.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value()) &&
                !model.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            Organisaatio oppilaitosE = findClosestOppilaitos(entity);
            if (oppilaitosE == null) {
                LOG.warn("Oppilaitos not found in parents");
                return null;
            }

            List<OrganisaatioSuhde> children = new ArrayList<OrganisaatioSuhde>();
            getDescendantSuhteet(oppilaitosE, children);
            int nextVal = children.size() + 1;

            String jarjNro ="99";
            int i;
            // kokeillaan aina seuraavaa numeroa kunnes vapaa toimipistekoodi löytyy
            for (i = nextVal; i<100; i++) {
                jarjNro = (i < 10) ? String.format("%s%s", "0", i) : String.format("%s", i);
                if (checkToimipistekoodiIsUniqueAndNotUsed(oppilaitosE.getOppilaitosKoodi() + jarjNro)) {
                    entity.setOpetuspisteenJarjNro(jarjNro);
                    return jarjNro;
                }
            }
            LOG.warn("Failed to generate opetuspisteenjarjnro (oppilaitoskoodi=" + oppilaitosE.getOppilaitosKoodi() + ")");
            return jarjNro;
        }
        return null;
    }

    private void getDescendantSuhteet(Organisaatio parentE,
            List<OrganisaatioSuhde> children) {
        if (parentE == null) {
            return;
        }
        List<OrganisaatioSuhde> curChildren = this.organisaatioSuhdeDAO.findBy("parent", parentE);
        if (curChildren != null) {
            for (OrganisaatioSuhde curChildSuhde : curChildren) {
                if (curChildSuhde.getSuhdeTyyppi()==OrganisaatioSuhde.OrganisaatioSuhdeTyyppi.HISTORIA) {
                    getDescendantSuhteet(curChildSuhde.getChild(), children);
                    children.add(curChildSuhde);
                }
            }
        }

    }

    /**
     * This is called when new organisaatio is saved - so there cannot be any
     * existing ytunnus.
     *
     * @param ytunnus
     */
    private void checkYtunnusIsUniqueAndNotUsed(String ytunnus) {
        if (ytunnus != null && !organisaatioDAO.isYtunnusAvailable(ytunnus)) {
            throw new YtunnusException();
        }
    }

    private void checkOrganisaatioHierarchy(Organisaatio organisaatio, String parentOid) {
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

    private void checkLakkautusAlkavatKoulutukset(Organisaatio entity) {
        if (organisaatioKoulutukset.alkaviaKoulutuksia(entity.getOid(), entity.getLakkautusPvm())) {
            throw new OrganisaatioLakkautusKoulutuksiaException();
        }
    }

    private void updateKoodisto(Organisaatio entity) {
        if (organisaatioKoodisto.paivitaKoodisto(entity)==false) {
            // TODO: Saa olla opportunistinen kunnes kokonaan toteutettu ja testattu. Sitten voidaan ehkä
            // heittää poikkeus jos koodiston päivitys ei onnistu.
            //throw new OrganisaatioKoodistoException();
            LOG.warn("Failed to update Koodisto.");
        }
    }

    private void createParentPath(Organisaatio entity, String parentOid) {
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

    private Organisaatio findClosestOppilaitos(Organisaatio orgE) {
        if (orgE.getParentOidPath() == null) {
            return null;
        }

        String[] ancestorOids = orgE.getParentOidPath().split(parentSplitter);
        for (int i = ancestorOids.length - 1; i >= 0; --i) {
            Organisaatio curOrgE = this.organisaatioDAO.findByOid(ancestorOids[i]);
            if (curOrgE != null
                    && curOrgE.getTyypit() != null
                    && curOrgE.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
                return curOrgE;
            }
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

    private void checkVersionInKoodistoUris(OrganisaatioRDTO model) {
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

}
