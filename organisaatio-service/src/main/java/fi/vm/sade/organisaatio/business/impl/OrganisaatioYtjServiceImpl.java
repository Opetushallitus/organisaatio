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

import fi.vm.sade.oid.OIDService;
import fi.vm.sade.oid.NodeClassCode;
import fi.vm.sade.organisaatio.ValidationException;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioStatus;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioViestinta;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.business.exception.AliorganisaatioLakkautusKoulutuksiaException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioDateException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioMuokkausTiedotDTO;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.YtjPaivitysLokiRepository;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.YTJResource;
import fi.vm.sade.organisaatio.service.util.OrganisaatioNimiUtil;
import fi.vm.sade.organisaatio.ytj.api.YTJDTO;
import fi.vm.sade.organisaatio.ytj.service.YtjDtoMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Service("organisaatioYtjService")
@Transactional(rollbackFor = Throwable.class)
public class OrganisaatioYtjServiceImpl implements OrganisaatioYtjService {

    private static final Pattern PUHELIN_VALIDATION = Pattern.compile(Puhelinnumero.VALIDATION_REGEXP);

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    protected YtjPaivitysLokiRepository ytjPaivitysLokiRepository;

    @Autowired
    private OIDService oidService;

    @Autowired
    private YTJResource ytjResource;

    @Autowired
    private OrganisaatioBusinessChecker checker;

    @Autowired
    private OrganisaatioKoodisto organisaatioKoodisto;

    private static Validator validator;

    @Autowired
    private OrganisaatioBusinessService businessService;

    @Autowired
    private OrganisaatioViestinta organisaatioViestinta;

    private YtjPaivitysLoki ytjPaivitysLoki;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final String POSTIOSOITE_PREFIX = "posti_";
    public static final String KIELI_KOODI_FI = "kieli_fi#1";
    public static final String KIELI_KOODI_SV = "kieli_sv#1";
    public static final String ORG_KIELI_KOODI_FI = "oppilaitoksenopetuskieli_1#1";
    public static final String ORG_KIELI_KOODI_SV = "oppilaitoksenopetuskieli_2#1";
    public static final int SEARCH_LIMIT = 10000;
    public static final int PARTITION_SIZE = 1000;

    public OrganisaatioYtjServiceImpl() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
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
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, varhaiskasvatuksen_jarjestaja, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioRepository.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioRepository.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioRepository.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        oidList.addAll(organisaatioRepository.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.VARHAISKASVATUKSEN_JARJESTAJA));
        if(oidList.isEmpty()) {
            LOG.error("päivitettävien organisaatioiden oidList on tyhjä, organisaatioita ei päivitetty");
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("ilmoitukset.log.virhe.tietokanta");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Fill the Y-tunnus list and parse off organisaatios that are lakkautettu
        Map<String, Organisaatio> organisaatiosByYtunnus = new HashMap<>();
        mapOrgsByYtunnusAndRemovePassiveOrgs(oidList, organisaatiosByYtunnus);

        List<YTJDTO> ytjdtoList = fetchDataFromYtj(new ArrayList<>(organisaatiosByYtunnus.keySet()));
        if(ytjdtoList.isEmpty()) {
            LOG.error("YTJ-tietoja ei saatu haettua, päivitys keskeytetään.");
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("ilmoitukset.log.virhe.ytjyhteys");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Update from YTJ data and get list of updated organisations
        List<Organisaatio> updateOrganisaatioList = doUpdate(ytjdtoList, organisaatiosByYtunnus, forceUpdate);

        // Update listed organisations to db and koodisto service.
        for(Iterator<Organisaatio> iterator = updateOrganisaatioList.iterator(); iterator.hasNext();) {
            Organisaatio organisaatio = iterator.next();
            try {
                Set<ConstraintViolation<Organisaatio>> constraintViolations = validator.validate(organisaatio);
                if(constraintViolations.size() > 0) {
                    throw new ValidationException(constraintViolations.iterator().next().getMessage());
                }
                // update koodisto (When name has changed)
                try {
                    organisaatioKoodisto.paivitaKoodisto(organisaatio);
                } catch (Exception e) {
                    LOG.error("Organisaation " + organisaatio.getOid() + " päivitys koodistoon epäonnistui", e);
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

        ytjPaivitysLoki.setPaivitetytLkm(updateOrganisaatioList.size());
        ytjPaivitysLokiRepository.save(ytjPaivitysLoki);
        organisaatioViestinta.sendPaivitysLokiViestintaEmail(ytjPaivitysLoki);

        return ytjPaivitysLoki;
    }

    // Returns a list uf updated organisations. Cumulates errors to ytjPaivitysLoki field.
    private List<Organisaatio> doUpdate(List<YTJDTO> ytjOrganisaatios, Map<String, Organisaatio> organisaatiosByYtunnus, boolean forceUpdate) {
        List<Organisaatio> updatedOrganisaatios = new ArrayList<>();
        for(YTJDTO ytjOrg : ytjOrganisaatios) {
            Organisaatio organisaatio = organisaatiosByYtunnus.get(ytjOrg.getYtunnus().trim());
            if (updateOrg(ytjOrg, organisaatio, forceUpdate)) {
                organisaatio.setYtjPaivitysPvm(new Date());
                updatedOrganisaatios.add(organisaatio);
            }
        }
        return updatedOrganisaatios;
    }

    // validates, updates if needed and returns info if org was updated or not
    private boolean updateOrg(final YTJDTO ytjOrg, Organisaatio organisaatio, boolean forceUpdate) {
        boolean updateYtjKieli = false;
        boolean updateNimi = false;
        boolean updateOsoite = false;
        boolean updateSahkoposti = false;
        boolean updatePuhelin = false;
        boolean updateWww = false;
        boolean updateAlkupvm = false;
        boolean updateYritysmuoto = false;
        // don't update and notify if organisation is inactive in YTJ
        if (ytjOrg.getYritysTunnus().getYritysLopetettu()) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.LOPPUPVM, "ilmoitukset.log.virhe.lopetettu");
            return false;
        }
        // validate YTJ language and add lang if needed
        if(ytjOrg.getYrityksenKieli() == null) {
            // don't update anything if YTJ lang is missing (shouldn't be possible)
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.KIELI, "ilmoitukset.log.virhe.kieli.puuttuu");
            return false;
        }

        boolean kieliAddedFromYTJ = updateLangFromYTJ(ytjOrg, organisaatio);
        String ytjKielikoodi = getKielikoodiFromYTJlang(ytjOrg.getYrityksenKieli());
        if (!ytjKielikoodi.equals(organisaatio.getYtjKieli())) {
            organisaatio.setYtjKieli(ytjKielikoodi);
            updateYtjKieli = true;
        }
        // validate and update YTJ alkupvm
        Date ytjAlkupvm = validateandParseYtjAlkupvm(ytjOrg, organisaatio);
        if (ytjAlkupvm != null && (!ytjAlkupvm.equals(organisaatio.getAlkuPvm()) || forceUpdate)) {
            organisaatio.setAlkuPvm(ytjAlkupvm);
            updateAlkupvm = true;
        }
        // validate and update organisaatio's name and name history
        if(validateOrgName(organisaatio)) {
            // general validations for YTJ name and date parsing
            Date ytjNameAlkupvm = validateYtjName(ytjOrg, organisaatio);
            // check that YTJ name passes name history criteria
            if(ytjNameAlkupvm != null && validateNameHistoryForYtjName(organisaatio, ytjNameAlkupvm)) {
                updateNimi = updateNimiForOrg(ytjOrg, organisaatio, forceUpdate, ytjNameAlkupvm);
            }
        }
        if(validateYtjOsoite(ytjOrg, organisaatio)) {
            // Find osoite with right language (finnish or swedish)
            Osoite osoite = organisaatio.getPostiosoiteByKieli(ytjKielikoodi);
            if (osoite == null) {
                // No matching osoite found from organisation so we will create an empty one to be fetched from YTJ.
                osoite = new Osoite();
                if(!initYhteystietoforOrg(osoite, organisaatio, ytjKielikoodi, YtjVirhe.YTJVirheKohde.OSOITE, "ilmoitukset.log.virhe.oid.osoite")) {
                    osoite = null;
                }
            }
            updateOsoite = updateOsoiteFromYTJToOrganisaatio(ytjOrg, osoite, forceUpdate);
        }
        if (validateYtjSahkoposti(ytjOrg)) {
            Email email = organisaatio.getEmail(ytjKielikoodi);
            if (email == null) {
                email = new Email();
                if (!initYhteystietoforOrg(email, organisaatio, ytjKielikoodi, YtjVirhe.YTJVirheKohde.SAHKOPOSTI, "ilmoitukset.log.virhe.oid.sahkoposti")) {
                    email = null;
                }
            }
            updateSahkoposti = updateSahkopostiFromYTJToOrganisation(ytjOrg, email, forceUpdate);
        }
        if (validateYtjPuhelin(ytjOrg)) {
            Puhelinnumero puhelinnumero = organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN, ytjKielikoodi);
            if (puhelinnumero == null) {
                puhelinnumero = new Puhelinnumero();
                if(!initYhteystietoforOrg(puhelinnumero, organisaatio, ytjKielikoodi, YtjVirhe.YTJVirheKohde.PUHELIN, "ilmoitukset.log.virhe.oid.puhelin")) {
                    puhelinnumero = null;
                }
            }
            updatePuhelin = updatePuhelinFromYTJtoOrganisaatio(ytjOrg, puhelinnumero, forceUpdate);
        }
        if(validateYtjWww(ytjOrg)) {
            Www www = organisaatio.getWww(ytjKielikoodi);
            if(www == null) {
                www = new Www();
                if(!initYhteystietoforOrg(www, organisaatio, ytjKielikoodi, YtjVirhe.YTJVirheKohde.WWW, "ilmoitukset.log.virhe.oid.www")) {
                    www = null;
                }
            }
            updateWww = updateWwwFromYTJToOrganisation(ytjOrg, www, forceUpdate);
        }
        if (ytjOrg.getYritysmuoto() != null && !ytjOrg.getYritysmuoto().equals(organisaatio.getYritysmuoto())) {
            organisaatio.setYritysmuoto(ytjOrg.getYritysmuoto());
            updateYritysmuoto = true;
        }
        // validate and update contact info according to YTJ lang
        return (kieliAddedFromYTJ || updateYtjKieli || updateNimi || updateOsoite || updateSahkoposti || updatePuhelin || updateWww || updateAlkupvm || updateYritysmuoto);
    }

    /* Date related stuff */

    private Date validateandParseYtjAlkupvm(YTJDTO ytjOrg, final Organisaatio organisaatio) {
        if(ytjOrg.getYritysTunnus() == null || ytjOrg.getYritysTunnus().getAlkupvm() == null) {
            return null;
        }
        final Date ytunnusAlkupvm = parseDate(ytjOrg.getYritysTunnus().getAlkupvm(), organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "ilmoitukset.log.virhe.alkupvm.parse");
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
            LOG.info("YTJ y-tunnuksen alkupvm ei läpäise organisaatiopalvelun tarkistuksia " + organisaatio.getOid());
            return null;
        } catch (AliorganisaatioLakkautusKoulutuksiaException ke) {
            // this can't actually happen because we don't import end date, so no ytj error logging
            // but we still have to process the error that the validation method throws
            LOG.info("YTJ:ssä y-tunnuksella on loppupäivämäärä organisaatiolle {} jolla on alkavia koulutuksia",
                    organisaatio.getOid());
            return null;
        }
        return ytunnusAlkupvm;
    }

    /* Name related stuff*/

    private boolean validateNameHistoryForYtjName(Organisaatio organisaatio, Date ytjNameAlkupvm) {
        // In case updating from ytj would violate organisation service rule that current nimi must be the newest one,
        // we do not update nimi
        for(OrganisaatioNimi organisaatioNimi : organisaatio.getNimet()) {
            // if name exists for same or later alkupvm, do not update name history.
            if(organisaatioNimi.getAlkuPvm().equals(ytjNameAlkupvm)) {
                return false;
            } else if (organisaatioNimi.getAlkuPvm().after(ytjNameAlkupvm)) {
                LOG.info("YTJ:ssä on vanhempi nimitieto organisaatiolle " + organisaatio.getOid());
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
            } else if (ytjOrg.getSvNimi().length() > 100) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.svpitka");
                return null;
            }
        } else {
            if (ytjOrg.getNimi() == null) {
                logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "ilmoitukset.log.virhe.nimi.puuttuu.ytj");
                return null;
            } else if (ytjOrg.getNimi().length() > 100) {
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

    // update name and name history if needed
    private boolean updateNimiForOrg(final YTJDTO ytjOrg, final Organisaatio organisaatio, boolean forceUpdate, Date ytjNameAlkupvm) {
        boolean updateNimi = false;
        if(YTJLangIsSwedish(ytjOrg)) {
            if(organisaatio.getNimi().getString("sv") == null) {
                // just add new name, don't add entry to name history
                updateNimiWithNewKieli(ytjOrg.getSvNimi(), "sv", organisaatio, ytjNameAlkupvm);
                updateNimi = true;
            } else if (!(organisaatio.getNimi().getString("sv").equals(ytjOrg.getSvNimi())) || forceUpdate) {
                if(organisaatio.getNimi().getString("sv").equalsIgnoreCase(ytjOrg.getSvNimi())) {
                    // if only case changed, just update the name string and date
                    organisaatio.getNimi().addString("sv", ytjOrg.getSvNimi());
                    organisaatio.getCurrentNimi().setAlkuPvm(ytjNameAlkupvm);
                }
                else {
                    // update name history too
                    addNameToOrg(ytjOrg.getSvNimi(), "sv", "fi", organisaatio, ytjNameAlkupvm);
                }
                updateNimi = true;
            }
        } else {
            if(organisaatio.getNimi().getString("fi") == null) {
                // just add new name, don't add entry to name history
                updateNimiWithNewKieli(ytjOrg.getNimi(), "fi", organisaatio, ytjNameAlkupvm);
                updateNimi = true;
            } else if (!(organisaatio.getNimi().getString("fi").equals(ytjOrg.getNimi())) || forceUpdate) {
                if(organisaatio.getNimi().getString("fi").equalsIgnoreCase(ytjOrg.getNimi())) {
                    // if only case changed, just update the name string and date
                    organisaatio.getNimi().addString("fi", ytjOrg.getNimi());
                    organisaatio.getCurrentNimi().setAlkuPvm(ytjNameAlkupvm);
                } else {
                    // update name history too
                    addNameToOrg(ytjOrg.getNimi(), "fi", "sv", organisaatio, ytjNameAlkupvm);
                }
                updateNimi = true;
            }
        }
        return updateNimi;
    }

    private void addNameToOrg(final String name, final String lang, String anotherlang, Organisaatio organisaatio, Date ytjNameAlkupvm) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.setOrganisaatio(organisaatio);
        final MonikielinenTeksti newNimi = new MonikielinenTeksti();
        newNimi.setValues(new HashMap<String, String>() {{put(lang, name);}});
        if(organisaatio.getNimi().getString(anotherlang) != null) {
            // keep the existing name in the other lang
            newNimi.addString(anotherlang, organisaatio.getNimi().getValues().get(anotherlang));
        }
        organisaatio.setNimihaku(OrganisaatioNimiUtil.createNimihaku(newNimi));
        organisaatioNimi.setAlkuPvm(ytjNameAlkupvm);
        organisaatioNimi.setNimi(newNimi);
        organisaatio.setNimi(newNimi);
        // add to name history
        organisaatio.addNimi(organisaatioNimi);
    }

    private void updateNimiWithNewKieli(String nimi, String kieli, Organisaatio organisaatio, Date ytjNameAlkupvm) {
        organisaatio.getNimi().addString(kieli, nimi);
        organisaatio.setNimihaku(OrganisaatioNimiUtil.createNimihaku(organisaatio.getNimi()));
        organisaatio.getCurrentNimi().setAlkuPvm(ytjNameAlkupvm);
    }

    /* Lang related stuff */

    // Adds the missing language information to Organisaatio according to the YTJ language.
    private boolean updateLangFromYTJ(YTJDTO ytjdto, Organisaatio organisaatio) {
        String YTJKieli = ytjdto.getYrityksenKieli();
        if(YTJKieli != null && !YTJKieli.trim().isEmpty()) {
            boolean YTJkieliExists = false;
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

    private String getKielikoodiFromYTJlang(String yrityksenKieli) {
        if(yrityksenKieli.equals(YtjDtoMapperHelper.KIELI_SV)) {
            return KIELI_KOODI_SV;
        } else {
            return KIELI_KOODI_FI;
        }
    }

    private boolean YTJLangIsSwedish(YTJDTO ytjdto) {
        return ytjdto.getYrityksenKieli() != null
                && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV);
    }

    /* Contact info related stuff */

    private boolean validateYtjOsoite(YTJDTO ytjOrg, Organisaatio organisaatio) {
        if(ytjOrg.getPostiOsoite() == null || ytjOrg.getPostiOsoite().getKatu() == null) {
            // osoite can't be null but in YTJ there are organisations with only postinumero
            LOG.warn("YTJ:ssä ei osoitetta organisaatiolle " + organisaatio.getOid() + " / " + ytjOrg.getYtunnus());
            return false;
        }
        else if((ytjOrg.getPostiOsoite().getKatu() != null
                && ytjOrg.getPostiOsoite().getKatu().length() > 100) ||
                (ytjOrg.getPostiOsoite().getToimipaikka() != null
                && ytjOrg.getPostiOsoite().getToimipaikka().length() > 100) ||
                (ytjOrg.getPostiOsoite().getPostinumero() != null
                && ytjOrg.getPostiOsoite().getPostinumero().length() > 100)) {
            logYtjError(organisaatio, YtjVirhe.YTJVirheKohde.OSOITE, "ilmoitukset.log.virhe.osoite.pitka");
            LOG.error("YTJ:ssä liian pitkä osoite organisaatiolle " + organisaatio.getOid());
            return false;
        }
        return true;
    }

    private boolean validateYtjSahkoposti(YTJDTO ytjdto) {
        if (ytjdto.getSahkoposti() == null) {
            return false;
        }
        boolean valid = Email.isValid(ytjdto.getSahkoposti());
        if (!valid) {
            LOG.warn("YTJ:ssä organisaatiolla " + ytjdto.getYtunnus() +" virheellinen sähköpostiosoite '" + ytjdto.getSahkoposti() + "'");
        }
        return valid;
    }

    private boolean validateYtjPuhelin(YTJDTO ytjOrg) {
        String puhelin = ytjOrg.getPuhelin();
        if (puhelin == null || puhelin.length() > 100) {
            return false;
        } else {
            ytjOrg.setPuhelin(puhelin.split(",|; *")[0]); // huh? mitä/miksi?!
        }

        return PUHELIN_VALIDATION.matcher(ytjOrg.getPuhelin()).matches();
    }

    private boolean validateYtjWww(YTJDTO ytjOrg) {
        if(ytjOrg.getWww() == null) {
            return false;
        }
        else if(ytjOrg.getWww().length() > 100) {
            return false;
        }
        else {
            // http://-prefix check and fix.
            ytjOrg.setWww(fixHttpPrefix(ytjOrg.getWww()));
        }
        return true;
    }

    // set the common fields for all Yhteystietos
    private boolean initYhteystietoforOrg(Yhteystieto yhteystieto, Organisaatio organisaatio, String kielikoodi, YtjVirhe.YTJVirheKohde virhekohde, String virheviesti) {
        String oid = generateOid();
        if (oid != null) {
            yhteystieto.setKieli(kielikoodi);
            yhteystieto.setOrganisaatio(organisaatio);
            yhteystieto.setYhteystietoOid(oid);
            organisaatio.addYhteystieto(yhteystieto);
            return true;
        } else {
            logYtjError(organisaatio, virhekohde, virheviesti);
            LOG.error("Could not generate oid for yhteystieto, skipping the field for " + organisaatio.getOid());
            return false;
        }
    }

    private boolean  updateOsoiteFromYTJToOrganisaatio(YTJDTO ytjdto, Osoite osoite, final boolean forceUpdate) {
        if(osoite == null) {
            return false;
        }
        boolean update = false;
        osoite.setOsoiteTyyppi(Osoite.TYYPPI_POSTIOSOITE);
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

    private boolean updatePuhelinFromYTJtoOrganisaatio(YTJDTO ytjOrg, Puhelinnumero puhelinnumero, boolean forceUpdate) {
        boolean update = false;
        puhelinnumero.setTyyppi(Puhelinnumero.TYYPPI_PUHELIN);
        // Update puhelinnumero from YTJ if it missmatches the current one.
        if (!ytjOrg.getPuhelin().equals(puhelinnumero.getPuhelinnumero()) || forceUpdate) {
            puhelinnumero.setPuhelinnumero(ytjOrg.getPuhelin());
            update = true;
        }
        return update;
    }

    private boolean updateWwwFromYTJToOrganisation(YTJDTO ytjdto, Www www, boolean forceUpdate) {
        boolean update = false;
        if(www == null) {
            return false;
        }
        // Update www from YTJ if it missmatches the current one.
        if(!ytjdto.getWww().equals(www.getWwwOsoite()) || forceUpdate) {
            www.setWwwOsoite(ytjdto.getWww());
            update = true;
        }
        return update;
    }

    private boolean updateSahkopostiFromYTJToOrganisation(YTJDTO ytjdto, Email email, boolean forceUpdate) {
        boolean update = false;
        if (email == null) {
            return false;
        }
        if (!ytjdto.getSahkoposti().equals(email.getEmail()) || forceUpdate) {
            email.setEmail(ytjdto.getSahkoposti());
            update = true;
        }
        return update;
    }

    /* Other help methods */

    private List<YTJDTO> fetchDataFromYtj(List<String> ytunnusList) {
        List<YTJDTO> ytjdtoList = new ArrayList<>();
        for (int i = 0; i < ytunnusList.size(); i += PARTITION_SIZE) {
            try {
                // Fetch data from ytj for these organisations
                ytjdtoList.addAll(ytjResource.doYtjMassSearch(ytunnusList.subList(i, Math.min(i + PARTITION_SIZE, ytunnusList.size()))));
            } catch (OrganisaatioResourceException ore) {
                LOG.error("Virhe YTJ-tietojen haussa", ore);
            }
        }
        return ytjdtoList.stream().filter(dto -> dto.getYtunnus() != null).collect(toList());
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

    private void mapOrgsByYtunnusAndRemovePassiveOrgs(List<String> oidList, Map<String, Organisaatio> organisaatioMap) {
        List<Organisaatio> organisaatioList = organisaatioRepository.findByOidList(oidList.stream().distinct().collect(toList()), SEARCH_LIMIT);
        for(Organisaatio organisaatio : organisaatioList) {
            if(organisaatio.getStatus() == OrganisaatioStatus.AKTIIVINEN
                    || organisaatio.getStatus() == OrganisaatioStatus.SUUNNITELTU) {
                organisaatioMap.put(organisaatio.getYtunnus().trim(), organisaatio);
            }
        }
    }

    private String fixHttpPrefix(String www) {
        if(www != null && !www.matches("^(https?://).*$")) {
            www = "http://" + www;
        }
        return www;
    }

    private void htmlDecodeAmpInYtjNames(YTJDTO ytjdto) {
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
        } catch (Exception e) {
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
