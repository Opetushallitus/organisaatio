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

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.business.exception.AliorganisaatioLakkautusKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dao.YtjPaivitysLokiDao;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.model.dto.YTJErrorsDto;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.rajapinnat.ytj.api.YTJDTO;
import fi.vm.sade.rajapinnat.ytj.service.YtjDtoMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.OptimisticLockException;
import javax.validation.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("organisaatioYtjService")
@Transactional(rollbackFor = Throwable.class, readOnly = true)
public class OrganisaatioYtjServiceImpl implements OrganisaatioYtjService {

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private YtjPaivitysLokiDao ytjPaivitysLokiDao;

    @Autowired
    private OIDService oidService;

    @Autowired
    private YTJResource ytjResource;

    @Autowired
    private OrganisaatioBusinessChecker checker;

    @Autowired
    private OrganisaatioKoodisto organisaatioKoodisto;

    @Autowired
    private IndexerResource solrIndexer;

    private static Validator validator;
    
    @Autowired
    private OrganisaatioBusinessService businessService;

    @Autowired
    private OrganisaatioViestinta organisaatioViestinta;

    private YtjPaivitysLoki ytjPaivitysLoki;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final String POSTIOSOITE_PREFIX = "posti_";
    private static final String KIELI_KOODI_FI = "kieli_fi#1";
    private static final String KIELI_KOODI_SV = "kieli_sv#1";
    private static final String ORG_KIELI_KOODI_FI = "oppilaitoksenopetuskieli_1#1";
    private static final String ORG_KIELI_KOODI_SV = "oppilaitoksenopetuskieli_2#1";
    private static final int SEARCH_LIMIT = 10000;
    private static final int PARTITION_SIZE = 1000;

    public OrganisaatioYtjServiceImpl() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    // Returns a list uf updated organisations. Cumulates errors to ytjPaivitysLoki field.
    public List<Organisaatio> doUpdate(List<YTJDTO> ytjOrganisaatios, Map<String,Organisaatio> organisaatiosByYtunnus, boolean forceUpdate) {
        List<Organisaatio> updatedOrganisaatios = new ArrayList<>();
        for(YTJDTO ytjOrg : ytjOrganisaatios) {
            Organisaatio organisaatio = organisaatiosByYtunnus.get(ytjOrg.getYtunnus().trim());
            if(updateOrg(ytjOrg, organisaatio, forceUpdate)) {
                organisaatio.setYtjPaivitysPvm(new Date());
                updatedOrganisaatios.add(organisaatio);
            }
        }
        return updatedOrganisaatios;
    }

    // validates, updates if needed and returns info if org was updated or not
    private boolean updateOrg(final YTJDTO ytjOrg, Organisaatio organisaatio, boolean forceUpdate) {
        boolean updateNimi = false;
        boolean updateOsoite = false;
        boolean updatePuhelin = false;
        boolean updateWww = false;
        boolean updateAlkupvm = false;
        // validate YTJ language and add lang if needed
        if(ytjOrg.getYrityksenKieli() == null) {
            // don't update anything if YTJ lang is missing (shouldn't be possible)
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.KIELI, "ilmoitukset.log.virhe.kieli.puuttuu");
            return false;
        }
        boolean updateKieli = updateLangFromYTJ(ytjOrg, organisaatio);
        // validate and update YTJ alkupvm
        Date ytjAlkupvm = validateandParseYtjAlkupvm(ytjOrg, organisaatio);
        if (ytjAlkupvm != null && (!ytjAlkupvm.equals(organisaatio.getAlkuPvm()) || forceUpdate)) {
            organisaatio.setAlkuPvm(ytjAlkupvm);
            updateAlkupvm = true;
        }
        // validate organisaatio's name and name history
        if(validateOrgName(organisaatio)) {
            // general validations for YTJ name and date parsing
            Date ytjNameAlkupvm = validateYtjName(ytjOrg, organisaatio);
            // check that YTJ name passes name history criteria
            if(ytjNameAlkupvm != null && validateNameHistoryForYtjName(organisaatio, ytjNameAlkupvm)) {
                // only update if values are different or force update
                if(YTJLangIsSwedish(ytjOrg) && !ytjOrg.getSvNimi().equals(organisaatio.getNimi().getString("sv"))
                        || !YTJLangIsSwedish(ytjOrg) && !ytjOrg.getNimi().equals(organisaatio.getNimi().getString("fi"))
                        || forceUpdate) {
                    // create new name
                    OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
                    organisaatioNimi.setOrganisaatio(organisaatio);
                    MonikielinenTeksti newNimi = new MonikielinenTeksti();
                    if(YTJLangIsSwedish(ytjOrg)) {
                        newNimi.setValues(new HashMap<String, String>() {{put("sv", ytjOrg.getSvNimi());}});
                        organisaatio.setNimihaku(ytjOrg.getSvNimi());
                    }
                    else {
                        newNimi.setValues(new HashMap<String, String>() {{put("fi", ytjOrg.getNimi());}});
                        organisaatio.setNimihaku(ytjOrg.getNimi());
                    }
                    organisaatioNimi.setAlkuPvm(ytjNameAlkupvm);
                    organisaatioNimi.setNimi(newNimi);
                    organisaatio.setNimi(newNimi);
                    // add to name history
                    organisaatio.addNimi(organisaatioNimi);
                    updateNimi = true;
                }
            }
        }
        if(validateYtjOsoite(ytjOrg)) {
            updateOsoite = true;
        }
        if(validateYtjPuhelin(ytjOrg)) {
            updatePuhelin = true;
        }
        if(validateYtjWww(ytjOrg)) {
            updateWww = true;
        }
        // validate and update contact info according to YTJ lang
        return (updateKieli || updateNimi || updateOsoite || updatePuhelin || updateWww || updateAlkupvm);
    }

    private boolean validateYtjOsoite(YTJDTO ytjOrg) {
        if(ytjOrg.getPostiOsoite() == null) {
            return false;
        }
        else if(ytjOrg.getPostiOsoite().getKatu() != null
                && ytjOrg.getPostiOsoite().getKatu().length() > ValidationConstants.GENERIC_MAX) {
            return false;
        }
        else if(ytjOrg.getPostiOsoite().getToimipaikka() != null
                && ytjOrg.getPostiOsoite().getToimipaikka().length() > ValidationConstants.GENERIC_MAX) {
            return false;
        }
        else if(ytjOrg.getPostiOsoite().getPostinumero() != null
                && ytjOrg.getPostiOsoite().getPostinumero().length() > ValidationConstants.GENERIC_MAX) {
            return false;
        }
        return true;
    }

    private boolean validateYtjPuhelin(YTJDTO ytjOrg) {
        if(ytjOrg.getPuhelin() == null) {
            return false;
        }
        else if(ytjOrg.getPuhelin().length() > ValidationConstants.GENERIC_MAX) {
            return false;
        }
        else {
            // Parse extra stuff off.
            ytjOrg.setPuhelin(ytjOrg.getPuhelin().split(",|; *")[0]);
        }
        return true;
    }

    private boolean validateYtjWww(YTJDTO ytjOrg) {
        if(ytjOrg.getWww() == null) {
            return false;
        }
        else if(ytjOrg.getWww().length() > ValidationConstants.GENERIC_MAX) {
            return false;
        }
        else {
            // http://-prefix check and fix.
            ytjOrg.setWww(fixHttpPrefix(ytjOrg.getWww()));
        }
        return true;
    }

    private boolean validateNameHistoryForYtjName(Organisaatio organisaatio, Date ytjNameAlkupvm) {
        // In case updating from ytj would violate organisation service rule that current nimi must be the newest one,
        // we do not update nimi
        for(OrganisaatioNimi organisaatioNimi : organisaatio.getNimet()) {
            // if name exists for same or later alkupvm, do not update name history.
            if(organisaatioNimi.getAlkuPvm().equals(ytjNameAlkupvm)) {
                return false;
            } else if (organisaatioNimi.getAlkuPvm().after(ytjNameAlkupvm)) {
                LOG.error("YTJ:ssä on vanhempi nimitieto organisaatiolle " + organisaatio.getOid());
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.vanha");
                return false;
            }
        }
        return true;
    }

    private Date validateYtjName(YTJDTO ytjOrg, Organisaatio organisaatio) {
        // basic validation
        if (YTJLangIsSwedish(ytjOrg)) {
            if (ytjOrg.getSvNimi() == null) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.svpuuttuu");
                return null;
            } else if (ytjOrg.getSvNimi().length() > ValidationConstants.GENERIC_MAX) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.svpitka");
                return null;
            }
        } else {
            if (ytjOrg.getNimi() == null) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.puuttuu.ytj");
                return null;
            } else if (ytjOrg.getNimi().length() > ValidationConstants.GENERIC_MAX) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.pitka");
                return null;
            }
        }
        // Fix encoded ampersand characters
        htmlDecodeAmpInYtjNames(ytjOrg);
        // parse alkupvm to date
        if (ytjOrg.getAloitusPvm() == null) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.alkupvm.puuttuu");
            return null;
        } else {
            return parseDate(ytjOrg.getAloitusPvm(), organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.alkupvm.parse");
        }
    }

    private boolean validateOrgName(Organisaatio organisaatio) {
        // validate nimi
        if(organisaatio.getNimi() == null) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.puuttuu");
            LOG.error("Organisaation " + organisaatio.getOid() + " nimi puuttuu organisatiopalvelussa. Tietoja ei päivitetty.");
            return false;
        }
        // In case nimihistoria is empty it is handled when updating.
        if(!organisaatio.getNimet().isEmpty()) {
            // name history validation checks and handle
            try {
                checker.checkNimihistoriaAlkupvm(organisaatio.getNimet());
                MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(organisaatio.getNimet());
                if (nimi == null) {
                    throw new OrganisaatioNameHistoryNotValidException();
                }

                // Tarkistetaan, että organisaatiolle asetettu nimi ei ole
                // ristiriidassa nimihistorian kanssa
                if (!nimi.getValues().equals(organisaatio.getNimi().getValues())) {
                    throw new OrganisaatioNameHistoryNotValidException();
                }
            } catch (OrganisaatioNameHistoryNotValidException e) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.historia");
                LOG.error("Virhe nimihistoriassa organisaatiolle " + organisaatio.getOid());
                return false;
            }
        }
        return true;
    }

    private Date validateandParseYtjAlkupvm(YTJDTO ytjOrg, final Organisaatio organisaatio) {
        if(ytjOrg.getYritysTunnus() == null || ytjOrg.getYritysTunnus().getAlkupvm() == null) {
            return null;
        }
        final Date ytunnusAlkupvm = parseDate(ytjOrg.getYritysTunnus().getAlkupvm(), organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "Y-tunnus");
        if(ytunnusAlkupvm == null) {
            return null;
        }
                final OrganisaatioMuokkausTiedotDTO tiedotDTO = new OrganisaatioMuokkausTiedotDTO() {{
                    setAlkuPvm(ytunnusAlkupvm);
                    setOid(organisaatio.getOid());
                }};
        try {
            businessService.batchValidatePvm(
                    new HashMap<String, OrganisaatioMuokkausTiedotDTO>() {{
                        put(tiedotDTO.getOid(), tiedotDTO);
                    }},
                    new HashMap<String, Organisaatio>() {{
                        put(organisaatio.getOid(), organisaatio);
                    }});
        } catch (OrganisaatioDateException de) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "ilmoitukset.log.virhe.alkupvm.tarkistukset");
            LOG.error("YTJ y-tunnuksen alkupvm ei läpäise organisaatiopalvelun tarkistuksia " + organisaatio.getOid());
            return null;
        } catch (AliorganisaatioLakkautusKoulutuksiaException ke) {
            // this can't actually happend because we don't import end date, so no ytj error logging
            // but we still have to process the error that the validation method throws
            LOG.error("YTJ:ssä y-tunnuksella on loppupäivämäärä organisaatiolle ",
                    organisaatio.getOid() + " jolla on alkavia koulutuksia");
            return null;
        }
        return ytunnusAlkupvm;
    }

    // Updates nimi and other info for all Koulutustoimija, Muu_organisaatio and Tyoelamajarjesto organisations using YTJ api
    @Override
    public YtjPaivitysLoki updateYTJData(final boolean forceUpdate) {
        ytjPaivitysLoki = new YtjPaivitysLoki();
        ytjPaivitysLoki.setPaivitysaika(new Date());
        ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT);

        // Create y-tunnus list of updateable arganisations
        List<String> oidList = new ArrayList<>();
        // Search the organisations using the DAO since it provides osoites.
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        if(oidList.isEmpty()) {
            LOG.error("päivitettävien organisaatioiden oidList on tyhjä, organisaatioita ei päivitetty");
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("ilmoitukset.log.virhe.tietokanta");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Fill the Y-tunnus list and parse off organisaatios that are lakkautettu
        Map<String,Organisaatio> organisaatiosByYtunnus = new HashMap<>();
        mapOrganisaatioListByYtunnus(oidList, organisaatiosByYtunnus);

        List<YTJDTO> ytjdtoList = fetchDataFromYtj(new ArrayList<>(organisaatiosByYtunnus.keySet()));
        if(ytjdtoList.isEmpty()) {
            LOG.error("YTJ-tietoja ei saatu haettua, päivitys keskeytetään.");
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("ilmoitukset.log.virhe.ytjyhteys");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Check which organisations need to be updated. YtjPaivitysPvm is the date when info is fetched from YTJ.
        List <Organisaatio> updateOrganisaatioList = doOldUglyUpdate(forceUpdate, organisaatiosByYtunnus, ytjdtoList);
        // Update listed organisations to db and koodisto service.
        for(Iterator<Organisaatio> iterator = updateOrganisaatioList.iterator(); iterator.hasNext();) {
            Organisaatio organisaatio = iterator.next();
            try {
                Set<ConstraintViolation<Organisaatio>> constraintViolations = validator.validate(organisaatio);
                if(constraintViolations.size() > 0) {
                    throw new ValidationException(constraintViolations.iterator().next().getMessage());
                }
                organisaatioDAO.updateOrg(organisaatio);
                // update koodisto (When name has changed)
                if(organisaatioKoodisto.paivitaKoodisto(organisaatio, false) != null) {
                    LOG.error("Organisaation " + organisaatio.getOid() + " päivitys koodistoon epäonnistui");
                    logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.KOODISTO, "ilmoitukset.log.virhe.koodisto");
                }
            } catch(ConstraintViolationException | ValidationException ve) {
                LOG.error("Validointivirhe organisaatiolle " + organisaatio.getOid());
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.VALIDOINTI, "ilmoitukset.log.virhe.validointi");
                iterator.remove();
            } catch (OptimisticLockException ole) {
                LOG.error("Java persistence exception with organisation " + organisaatio.getOid(), ole.getMessage());
                iterator.remove();
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.TALLENNUS, "ilmoitukset.log.virhe.tallennus");
            } catch (RuntimeException re) {
                LOG.error("Could not update organisation " + organisaatio.getOid(), re.getMessage());
                iterator.remove();
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.TUNTEMATON, "ilmoitukset.log.virhe.tuntematon");
            }
        }
        // Call this since the class is readOnly so it won't be called automatically by transaction manager.
        organisaatioDAO.flush();

        // Index the updated resources.
        solrIndexer.index(updateOrganisaatioList);

        ytjPaivitysLoki.setPaivitetytLkm(updateOrganisaatioList.size());
        ytjPaivitysLokiDao.insert(ytjPaivitysLoki);
        ytjPaivitysLokiDao.flush();

        organisaatioViestinta.sendPaivitysLokiViestintaEmail(ytjPaivitysLoki);

        return ytjPaivitysLoki;
    }

    private List<Organisaatio> doOldUglyUpdate(boolean forceUpdate, Map<String, Organisaatio> organisaatiosByYtunnus, List<YTJDTO> ytjdtoList) {
        List<Organisaatio> updateOrganisaatioList = new ArrayList<>();
        for (YTJDTO ytjdto : ytjdtoList) {
            Organisaatio organisaatio = organisaatiosByYtunnus.get(ytjdto.getYtunnus().trim());
            YTJErrorsDto ytjErrorsDto = new YTJErrorsDto();
            // some basic validation first (null checks, corner cases etc)
            validateOrganisaatioDataForYTJ(organisaatio, ytjdto, ytjErrorsDto);
            validateYTJData(organisaatio, ytjdto, ytjErrorsDto);
            // don't proceed to update if there's something wrong
            // collect info to some map structure
            if (ytjErrorsDto.organisaatioValid) {
                Boolean updateNimi = false;
                Boolean updateOsoite = false;
                Boolean updatePuhelin = false;
                Boolean updateWww = false;
                Boolean updateAlkupvm = false;
                Boolean updateKieli = false;
                // Update nimi
                if (ytjErrorsDto.nimiValid  || ytjErrorsDto.nimiSvValid) {
                    updateNimi = updateNameFromYTJ(ytjdto, organisaatio, forceUpdate, ytjErrorsDto.nimiHistory);
                }

                // Update Osoite
                if(ytjErrorsDto.osoiteValid) {
                    updateOsoite = updateOsoiteFromYTJToOrganisaatio(ytjdto, organisaatio, forceUpdate);
                }

                // Update puhelin
                if(ytjErrorsDto.puhelinnumeroValid) {
                    updatePuhelin = updatePuhelinFromYTJtoOrganisaatio(forceUpdate, ytjdto, organisaatio);
                }

                // Update www
                if(ytjErrorsDto.wwwValid) {
                    updateWww = updateWwwFromYTJToOrganisation(forceUpdate, ytjdto, organisaatio);
                }

                // Update alkupvm
                if(ytjErrorsDto.ytunnusPvmValid) {
                    updateAlkupvm = updateOrgAlkupvm(ytjdto, organisaatio, forceUpdate);
                }

                if(ytjErrorsDto.kieliValid) {
                    updateKieli = updateYtjkieliFromYtjToOrganisaatio(organisaatio, ytjdto);
                }

                if (updateNimi || updateOsoite || updatePuhelin || updateWww || updateAlkupvm || updateKieli) {
                    updateOrganisaatioList.add(organisaatio);
                }
            }
        }
        return updateOrganisaatioList;
    }

    private Boolean updateOrgAlkupvm(YTJDTO ytjdto, Organisaatio organisaatio, Boolean forceUpdate) {
        Boolean update = false;
        if(ytjdto.getYritysTunnus().getAlkupvm() == null) {
            return false;
        }
        Date ytunnusAlkupvm = parseDate(ytjdto.getYritysTunnus().getAlkupvm(), organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "ilmoitukset.log.virhe.alkupvm.parse");
        if (ytunnusAlkupvm != null && (!ytunnusAlkupvm.equals(organisaatio.getAlkuPvm()) || forceUpdate)) {
            organisaatio.setAlkuPvm(ytunnusAlkupvm);
            update = true;
        }
        return update;
    }



    private void validateOrganisaatioDataForYTJ(final Organisaatio organisaatio, YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {

        if (organisaatio == null) {
            ytjErrorsDto.organisaatioValid = false;
        }
        else {
            // Add new kieli to the organisation if there isn't one matching the YTJ kieli
            updateLangFromYTJ(ytjdto, organisaatio);

            // validate nimi
            if(organisaatio.getNimi() == null) {
                ytjErrorsDto.nimiValid = false;
                ytjErrorsDto.nimiSvValid = false;
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.puuttuu");
                LOG.error("Organisaation " + organisaatio.getOid() + " nimi puuttuu organisatiopalvelussa. Tietoja ei päivitetty.");
            }
            // In case nimihistoria is empty it is handled when updating.
            else if(!organisaatio.getNimet().isEmpty()) {
                // name history validation checks and handle
                try {
                    checker.checkNimihistoriaAlkupvm(organisaatio.getNimet());
                    MonikielinenTeksti nimi = OrganisaatioNimiUtil.getNimi(organisaatio.getNimet());
                    if (nimi == null) {
                        throw new OrganisaatioNameHistoryNotValidException();
                    }

                    // Tarkistetaan, että organisaatiolle asetettu nimi ei ole
                    // ristiriidassa nimihistorian kanssa
                    if (!nimi.getValues().equals(organisaatio.getNimi().getValues())) {
                        throw new OrganisaatioNameHistoryNotValidException();
                    }
                } catch (OrganisaatioNameHistoryNotValidException e) {
                    logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.historia");
                    ytjErrorsDto.nimiValid = false;
                    ytjErrorsDto.nimiSvValid = false;
                    LOG.error("Virhe nimihistoriassa organisaatiolle " + organisaatio.getOid());
                }
            }

            // validate osoite
            // Find osoite with right language (finnish or swedish)
            Osoite osoite = findOsoiteByLangAndTypeFromYhteystiedot(ytjdto, organisaatio);
            // No matching kieli found from organisation so we will create an empty one to be fetched from YTJ.
            // (organisation language could be eg. fi/sv (dual) or en which are not in YTJ)
            if (osoite == null && ytjdto.getPostiOsoite() != null) {
                if(!addOsoiteForOrgWithYtjLang(ytjdto, organisaatio)) {
                    ytjErrorsDto.osoiteValid = false;
                }
            }

            // validate puhelinnumero
            // Create new puhelinnumero if one does not exist
            if(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) == null && ytjdto.getPuhelin() != null) {
                String oid = generateOid();
                if (oid !=null) {
                    Puhelinnumero puhelinnumero = new Puhelinnumero();
                    puhelinnumero.setPuhelinnumero("123456789");
                    puhelinnumero.setTyyppi(Puhelinnumero.TYYPPI_PUHELIN);
                    puhelinnumero.setYhteystietoOid(oid);
                    puhelinnumero.setOrganisaatio(organisaatio);
                    if (YTJLangIsSwedish(ytjdto)) {
                        puhelinnumero.setKieli(KIELI_KOODI_SV);
                    } else {
                        puhelinnumero.setKieli(KIELI_KOODI_FI);
                    }
                    organisaatio.addYhteystieto(puhelinnumero);

                } else {
                    logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.PUHELIN, "ilmoitukset.log.virhe.oid.puhelin");
                    LOG.error("Oid-generointi puhelinnumerolle epäonnistui organisaatiolla " + organisaatio.getOid());
                    ytjErrorsDto.puhelinnumeroValid = false;
                }
            }

            // validate www
            // Create new www if one does not exist (and ytj has data)
            Www www = organisaatio.getWww();
            if(www == null && ytjdto.getWww() != null) {
                String oid = generateOid();
                if (oid != null) {
                    www = new Www();
                    www.setYhteystietoOid(oid);
                    www.setOrganisaatio(organisaatio);
                    // to avoid NPE in comparisons
                    www.setWwwOsoite("");
                    if (YTJLangIsSwedish(ytjdto)) {
                        www.setKieli(KIELI_KOODI_SV);
                    } else {
                        www.setKieli(KIELI_KOODI_FI);
                    }
                    organisaatio.addYhteystieto(www);
                } else {
                    logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.WWW, "ilmoitukset.log.virhe.oid.www");
                    LOG.error("Oid-generointi www-osoitteelle epäonnistui organisaatiolla " + organisaatio.getOid());
                    ytjErrorsDto.wwwValid = false;
                }
            }
        }
    }

    // Validates data coming from ytj so that update() does not need to worry about getting stuck on hibernate validation.
    private void validateYTJData(final Organisaatio organisaatio, final YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {
        if(ytjdto == null) {
            //is this even possible?
            ytjErrorsDto.organisaatioValid = false;
        }
        else {
            // nimi
            checkYtjNames(organisaatio, ytjdto, ytjErrorsDto);

            // osoite
            if(ytjdto.getPostiOsoite() == null) {
                ytjErrorsDto.osoiteValid = false;
            }
            else if(ytjdto.getPostiOsoite().getKatu() != null
                    && ytjdto.getPostiOsoite().getKatu().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.osoiteValid = false;
            }
            else if(ytjdto.getPostiOsoite().getToimipaikka() != null
                    && ytjdto.getPostiOsoite().getToimipaikka().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.osoiteValid = false;
            }
            else if(ytjdto.getPostiOsoite().getPostinumero() != null
                    && ytjdto.getPostiOsoite().getPostinumero().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.osoiteValid = false;
            }

            // puhelin
            if(ytjdto.getPuhelin() == null) {
                ytjErrorsDto.puhelinnumeroValid = false;
            }
            else if(ytjdto.getPuhelin().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.puhelinnumeroValid = false;
            }
            else {
                // Parse extra stuff off.
                ytjdto.setPuhelin(ytjdto.getPuhelin().split(",|; *")[0]);
            }

            // www
            if(ytjdto.getWww() == null) {
                ytjErrorsDto.wwwValid = false;
            }
            else if(ytjdto.getWww().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.wwwValid = false;
            }
            else {
                // http://-prefix check and fix.
                ytjdto.setWww(fixHttpPrefix(ytjdto.getWww()));
            }

            // organisaatio alkupvm
            if(ytjdto.getYritysTunnus() == null || ytjdto.getYritysTunnus().getAlkupvm() == null) {
                ytjErrorsDto.ytunnusPvmValid = false;
            }
            else {
                final Date ytunnusAlkupvm = parseDate(ytjdto.getYritysTunnus().getAlkupvm(), organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "Y-tunnus");
                if(ytunnusAlkupvm != null) {
                    final OrganisaatioMuokkausTiedotDTO tiedotDTO = new OrganisaatioMuokkausTiedotDTO() {{
                        setAlkuPvm(ytunnusAlkupvm);
                        setOid(organisaatio.getOid());
                    }};
                    try {
                        businessService.batchValidatePvm(
                                new HashMap<String, OrganisaatioMuokkausTiedotDTO>() {{
                                    put(tiedotDTO.getOid(), tiedotDTO);
                                }},
                                new HashMap<String, Organisaatio>() {{
                                    put(organisaatio.getOid(), organisaatio);
                                }});
                    } catch (OrganisaatioDateException de) {
                        logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "ilmoitukset.log.virhe.alkupvm.tarkistukset");
                        LOG.error("YTJ y-tunnuksen alkupvm ei läpäise organisaatiopalvelun tarkistuksia " + organisaatio.getOid());
                        ytjErrorsDto.ytunnusPvmValid = false;
                    } catch (AliorganisaatioLakkautusKoulutuksiaException ke) {
                        // this can't actually happend because we don't import end date, so no ytj error logging
                        // but we still have to process the error that the validation method throws
                        LOG.error("YTJ:ssä y-tunnuksella on loppupäivämäärä organisaatiolle ",
                                organisaatio.getOid() + " jolla on alkavia koulutuksia");
                        ytjErrorsDto.ytunnusPvmValid = false;
                    }
                }
            }
        }
    }

    private Boolean updateYtjkieliFromYtjToOrganisaatio(Organisaatio organisaatio, YTJDTO ytjdto) {
        // Validate ytjKieli
        if(ytjdto.getYrityksenKieli() == null) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.KIELI, "ilmoitukset.log.virhe.kieli.puuttuu");
            return false;
        }
         else if(YTJLangIsSwedish(ytjdto)) {
            organisaatio.setYtjKieli(KIELI_KOODI_SV);
        }
        else {
            // null check has already been done before this method call
            organisaatio.setYtjKieli(KIELI_KOODI_FI);
        }
        return true;
    }

    private void checkYtjNames(Organisaatio organisaatio, YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {
        if(YTJLangIsSwedish(ytjdto)) {
            if(ytjdto.getSvNimi() == null) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.svpuuttuu");
                ytjErrorsDto.nimiSvValid = false;
            }
            else if(ytjdto.getSvNimi().length() > ValidationConstants.GENERIC_MAX) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.svpitka");
                ytjErrorsDto.nimiSvValid = false;
            }
        } else {
            if(ytjdto.getNimi() == null) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.puuttuu.ytj");
                ytjErrorsDto.nimiValid = false;
            }
            else if(ytjdto.getNimi().length() > ValidationConstants.GENERIC_MAX) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.pitka");
                ytjErrorsDto.nimiValid = false;
            }
        }

        // Allow ampersand characters
        htmlDecodeAmpInYtjNames(ytjdto);
        // In case updating from ytj would violate organisation service rule that current nimi must be the newest one,
        // we do not update nimi
        if(ytjdto.getAloitusPvm() == null) {
            ytjErrorsDto.nimiHistory = false;
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.alkupvm.puuttuu");
        } else {
            Date ytjAlkupvm = parseDate(ytjdto.getAloitusPvm(), organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.alkupvm.parse");
            if (ytjAlkupvm == null) {
                ytjErrorsDto.nimiHistory = false;
            } else {
                // Nimi and nimihistory validation
                checkNameHistoryForYtj(organisaatio, ytjErrorsDto, ytjAlkupvm);
            }
        }
    }

    private void checkNameHistoryForYtj(Organisaatio organisaatio, YTJErrorsDto ytjErrorsDto, Date ytjAlkupvm) {
        for(OrganisaatioNimi organisaatioNimi : organisaatio.getNimet()) {
            // In case nimi alkupvm already exist or newer alkupvm exist, do not update name history.
            if(organisaatioNimi.getAlkuPvm().equals(ytjAlkupvm)) {
                ytjErrorsDto.nimiValid = false;
                ytjErrorsDto.nimiSvValid = false;
                ytjErrorsDto.nimiHistory = false;
            } else if (organisaatioNimi.getAlkuPvm().after(ytjAlkupvm)) {
                LOG.error("YTJ:ssä on vanhempi nimitieto organsaatiolle " + organisaatio.getOid());
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.vanha");
                ytjErrorsDto.nimiValid = false;
                ytjErrorsDto.nimiSvValid = false;
                ytjErrorsDto.nimiHistory = false;
            }
        }
    }

    private boolean updateWwwFromYTJToOrganisation(boolean forceUpdate, YTJDTO ytjdto, Organisaatio organisaatio) {
        boolean update = false;
        // Find the www from organisaatio
        Www www = organisaatio.getWww();
        // Update www from YTJ if it missmatches the current one.
        if(www.getWwwOsoite() != null && (!ytjdto.getWww().equals(www.getWwwOsoite()))
                || forceUpdate) {
            www.setWwwOsoite(ytjdto.getWww());
            update = true;
        }
        return update;
    }

    private boolean updatePuhelinFromYTJtoOrganisaatio(boolean forceUpdate, YTJDTO ytjdto, Organisaatio organisaatio) {
        boolean update = false;
        // Update puhelinnumero from YTJ if it missmatches the current one.
        if((organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) != null
                && (!ytjdto.getPuhelin().equals(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).getPuhelinnumero()))
                || forceUpdate)) {
            organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN).setPuhelinnumero(ytjdto.getPuhelin());
            update = true;
        }
        return update;
    }

    // Adds the missing language information to Organisaatio according to the YTJ language.
    private boolean updateLangFromYTJ(YTJDTO ytjdto, Organisaatio organisaatio) {
        String YTJKieli = ytjdto.getYrityksenKieli();
        if(YTJKieli != null) {
            Boolean YTJkieliExists = false;
            for (String kieli : organisaatio.getKielet()) {
                if (kieli.trim().equals(ORG_KIELI_KOODI_FI)
                        && YTJKieli.trim().equals("Suomi")) {
                    YTJkieliExists = true;
                }
                if (kieli.trim().equals(ORG_KIELI_KOODI_SV)
                        && YTJLangIsSwedish(ytjdto)) {
                    YTJkieliExists = true;
                }
            }
            if (!YTJkieliExists) {
                String newKieli;
                // have to generate a new list because organisaatio.getKielet is unmodifiable
                List<String> newKieliList = new ArrayList<>();
                if (YTJLangIsSwedish(ytjdto)) {
                    newKieli = ORG_KIELI_KOODI_SV;
                } else {
                    newKieli = ORG_KIELI_KOODI_FI;
                }
                newKieliList.addAll(organisaatio.getKielet());
                newKieliList.add(newKieli);
                organisaatio.setKielet(newKieliList);
                return true;
            }
        }
        return false;
    }

    private boolean updateOsoiteFromYTJToOrganisaatio(YTJDTO ytjdto, Organisaatio organisaatio, final boolean forceUpdate) {
        boolean update = false;
        // Find osoite with right language (finnish or swedish)
        Osoite osoite = findOsoiteByLangAndTypeFromYhteystiedot(ytjdto, organisaatio);
        if (ytjdto.getPostiOsoite() != null && ytjdto.getPostiOsoite().getPostinumero() != null
                && (!(POSTIOSOITE_PREFIX + ytjdto.getPostiOsoite().getPostinumero().trim()).equals(osoite.getPostinumero())
                || forceUpdate)) {
            osoite.setPostinumero(POSTIOSOITE_PREFIX + ytjdto.getPostiOsoite().getPostinumero().trim());
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

    private Boolean addOsoiteForOrgWithYtjLang(YTJDTO ytjdto, Organisaatio organisaatio) {
        String oid = generateOid();
        if(oid != null) {
            Osoite osoite;
            osoite = new Osoite();
            osoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
            osoite.setOrganisaatio(organisaatio);
            osoite.setYhteystietoOid(oid);
            if (YTJLangIsSwedish(ytjdto)) {
                osoite.setKieli(KIELI_KOODI_SV);
            } else {
                osoite.setKieli(KIELI_KOODI_FI);
            }
            organisaatio.addYhteystieto(osoite);
            return true;
        } else {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.OSOITE, "ilmoitukset.log.virhe.oid.osoite");
            LOG.error("Could not generate oid for osoite, skipping the field for " + organisaatio.getOid());
            return false;
        }
    }

    private boolean YTJLangIsSwedish(YTJDTO ytjdto) {
        return ytjdto.getYrityksenKieli() != null
                && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV);
    }

    private Osoite findOsoiteByLangAndTypeFromYhteystiedot(YTJDTO ytjdto, Organisaatio organisaatio) {
        Osoite osoite = null;
        for (Yhteystieto yhteystieto : organisaatio.getYhteystiedot()) {
            if (yhteystieto instanceof Osoite && yhteystieto.getKieli().trim().equals(KIELI_KOODI_SV)
                    && ytjdto.getYrityksenKieli() != null
                    && ytjdto.getYrityksenKieli().equals(YtjDtoMapperHelper.KIELI_SV)) {
                if(((Osoite) yhteystieto).getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE)) {
                    osoite = (Osoite) yhteystieto;
                    return osoite;
                }
            }
            if (yhteystieto instanceof Osoite && yhteystieto.getKieli().trim().equals(KIELI_KOODI_FI)) {
                if(((Osoite) yhteystieto).getOsoiteTyyppi().equals(Osoite.TYYPPI_POSTIOSOITE)) {
                    osoite = (Osoite) yhteystieto;
                    return osoite;
                }
            }
        }
        return osoite;
    }

    // Update nimi to Organisaatio from YTJ and handle the name history (nimet). Does not require nimi.get(lang) to exist.
    // TODO refactor to create new name and change references everywhere to point to this one leaving old as history entry
    // TODO instead of editing the current one and creating new history entry.
    private boolean updateNameFromYTJ(final YTJDTO ytjdto, final Organisaatio organisaatio, final boolean forceUpdate, Boolean updateNimiHistory) {
        boolean update = false;
        if((ytjdto.getNimi() != null && !ytjdto.getNimi().equals(organisaatio.getNimi().getString("fi")))
                || (ytjdto.getSvNimi() != null && !ytjdto.getSvNimi().equals(organisaatio.getNimi().getString("sv")))
                || ((ytjdto.getNimi() != null || ytjdto.getSvNimi() != null) && forceUpdate)) {
            OrganisaatioNimi currentOrgNimi = null;
            if(organisaatio.getNimi().getString("fi") != null || organisaatio.getNimi().getString("sv") != null) {
                for(OrganisaatioNimi orgNimi : organisaatio.getNimet()) {
                    if(orgNimi.getNimi() == organisaatio.getNimi()) {
                        currentOrgNimi = orgNimi;
                    }
                }
                // Update nimet (history) with a copy of the old current nimi (orgNimi)
                if(updateNimiHistory && currentOrgNimi != null) {
                    // Check equality in case of forceUpdate to prevent duplicates in name history.
                    if((ytjdto.getNimi() != null && currentOrgNimi.getNimi().getString("fi") != null
                            && !ytjdto.getNimi().equals(currentOrgNimi.getNimi().getString("fi")))
                            ||
                            (ytjdto.getSvNimi() != null && currentOrgNimi.getNimi().getString("sv") != null)
                                    && !ytjdto.getSvNimi().equals(currentOrgNimi.getNimi().getString("sv"))) {
                        // Create new entry to nimihistoria
                        OrganisaatioNimi newOrgNimi = createOrganisaatioNimiWithYTJLang(ytjdto, currentOrgNimi);
                        organisaatio.addNimi(newOrgNimi);
                    }
                }
                // If organisaatio (faultly) has no nimihistoria but organisaatio still has nimi create new entry
                // containing current information.
                if(organisaatio.getNimet().isEmpty()) {
                    OrganisaatioNimi organisaatioNimi = createOrganisaatioNimiFromYtjData(ytjdto, organisaatio);
                    organisaatio.addNimi(organisaatioNimi);
                    currentOrgNimi = organisaatioNimi;
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
            // When updating nimi always update alkupvm from YTJ as toiminimen alkupvm.
            if(ytjdto.getAloitusPvm() != null && currentOrgNimi != null && updateNimiHistory) {
                Date ytjAlkupvm = parseDate(ytjdto.getAloitusPvm(), organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.alkupvm.parse");
                currentOrgNimi.setAlkuPvm(ytjAlkupvm);
            }
            update = true;
        }
        return update;
    }

    private OrganisaatioNimi createOrganisaatioNimiFromYtjData(final YTJDTO ytjdto, Organisaatio organisaatio) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(organisaatio);
        MonikielinenTeksti newNimi = new MonikielinenTeksti();
        if(ytjdto.getSvNimi() != null) {
            newNimi.setValues(new HashMap<String, String>() {{put("sv", ytjdto.getSvNimi());}});
        }
        else {
            newNimi.setValues(new HashMap<String, String>() {{put("fi", ytjdto.getNimi());}});
        }
        organisaatioNimi.setNimi(newNimi);
        return organisaatioNimi;
    }

    private OrganisaatioNimi createOrganisaatioNimiWithYTJLang(YTJDTO ytjdto, final OrganisaatioNimi orgNimi) {
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
        return newOrgNimi;
    }

    private void logYtjError(Organisaatio organisaatio, YtjVirhe.YTJVirheKohde kohde, String viesti) {
        ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);
        YtjVirhe virhe = new YtjVirhe();
        virhe.setOid(organisaatio.getOid());
        String nimiFi = organisaatio.getNimi().getString("fi");
        if (nimiFi != null) {
            virhe.setOrgNimi(nimiFi);
        } else {
            virhe.setOrgNimi(organisaatio.getNimi().getString("sv"));
        }
        virhe.setVirhekohde(kohde);
        virhe.setVirheviesti(viesti);
        virhe.setYtjPaivitysLoki(ytjPaivitysLoki);
        ytjPaivitysLoki.getYtjVirheet().add(virhe);
    }

    private void mapOrganisaatioListByYtunnus(List<String> oidList, Map<String, Organisaatio> organisaatioMap) {
        List<Organisaatio> organisaatioList = organisaatioDAO.findByOidList(oidList, SEARCH_LIMIT);
        for(Organisaatio organisaatio : organisaatioList) {
            if(organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.AKTIIVINEN
                    || organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.SUUNNITELTU) {
                organisaatioMap.put(organisaatio.getYtunnus().trim(), organisaatio);
            }
        }
    }

    private List<YTJDTO> fetchDataFromYtj(List<String> ytunnusList) {
        List <YTJDTO> ytjdtoList = new ArrayList<>();
        for (int i = 0; i < ytunnusList.size(); i += PARTITION_SIZE) {
            try {
                // Fetch data from ytj for these organisations
                ytjdtoList.addAll(ytjResource.doYtjMassSearch(ytunnusList.subList(i, Math.min(i + PARTITION_SIZE, ytunnusList.size()))));
            } catch (OrganisaatioResourceException ore) {
                LOG.error("Virhe YTJ-tietojen haussa", ore);
            }
        }
        return ytjdtoList;
    }

    private String fixHttpPrefix(String www) {
        if(www != null && !www.matches("^(https?:\\/\\/).*$")) {
            www = "http://" + www;
        }
        return www;
    }

    private void htmlDecodeAmpInYtjNames(YTJDTO ytjdto) {
        // TODO this would be better to fix in validator (or get rid of html encoding in the backend)
        if(ytjdto.getNimi() != null) {
            ytjdto.setNimi(ytjdto.getNimi().replace("&amp;", "&"));
        }
        if(ytjdto.getSvNimi() != null) {
            ytjdto.setSvNimi(ytjdto.getSvNimi().replace("&amp;", "&"));
        }
    }

    private String generateOid() {
        try {
            return oidService.newOid(NodeClassCode.TEKN_5);
        } catch (ExceptionMessage e) {
            LOG.error("Oid-generointi yhteystiedolle epäonnistui", e);
        }
        return null;
    }

    private Date parseDate(String pvm, Organisaatio organisaatio, YtjVirhe.YTJVirheKohde kohde, String virheviesti) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            return format.parse(pvm);
        } catch (ParseException pe) {
            logYtjError(organisaatio, kohde, virheviesti);
            LOG.error("virheellinen päivämäärä kentällä " + kohde + " organisaatiolle " + organisaatio.getOid());
            return null;
        }
    }

}
