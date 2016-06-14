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

    // Updates nimi and other info for all Koulutustoimija, Muu_organisaatio and Tyoelamajarjesto organisations using YTJ api
    @Override
    public YtjPaivitysLoki updateYTJData(final boolean forceUpdate) {
        ytjPaivitysLoki = new YtjPaivitysLoki();
        ytjPaivitysLoki.setPaivitysaika(new Date());
        ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT);
        List<Organisaatio> updateOrganisaatioList = new ArrayList<>();

        // Create y-tunnus list of updateable arganisations
        List<String> oidList = new ArrayList<>();
        // Search the organisations using the DAO since it provides osoites.
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        if(oidList.isEmpty()) {
            LOG.debug("oidList is empty, no organisations updated from YTJ!");
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("päivitettävien organisaatioiden haku organisaatiopalvelusta epäonnistui");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Fill the Y-tunnus list and parse off organisaatios that are lakkautettu
        Map<String,Organisaatio> organisaatiosByYtunnus = new HashMap<>();
        mapOrganisaatioListByYtunnus(oidList, organisaatiosByYtunnus);

        List<YTJDTO> ytjdtoList = new ArrayList<>();
        try {
            // Fetch data from ytj for these organisations
            fetchDataFromYtj(new ArrayList<>(organisaatiosByYtunnus.keySet()), ytjdtoList);
        } catch(OrganisaatioResourceException ore) {
            LOG.error("Could not fetch ytj data. Aborting ytj data update.", ore);
            ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.EPAONNISTUNUT);
            ytjPaivitysLoki.setPaivitysTilaSelite("tietojen haku YTJ-palvelusta epäonnistui");
            ytjPaivitysLoki.setPaivitetytLkm(0);
            return ytjPaivitysLoki;
        }
        // Check which organisations need to be updated. YtjPaivitysPvm is the date when info is fetched from YTJ.
        for (YTJDTO ytjdto : ytjdtoList) {
            Organisaatio organisaatio = organisaatiosByYtunnus.get(ytjdto.getYtunnus().trim());
            YTJErrorsDto ytjErrorsDto = new YTJErrorsDto();
            // some basic validation first (null checks, corner cases etc)
            validateOrganisaatioDataForYTJ(organisaatio, ytjdto, ytjErrorsDto);
            validateYTJData(organisaatio, ytjdto, ytjErrorsDto, ytjPaivitysLoki);
            // don't proceed to update if there's something wrong
            // collect info to some map structure
            if (ytjErrorsDto.organisaatioValid) {
                Boolean updateNimi = false;
                Boolean updateOsoite = false;
                Boolean updatePuhelin = false;
                Boolean updateWww = false;
                Boolean updateAlkupvm = false;
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

                if (updateNimi || updateOsoite || updatePuhelin || updateWww || updateAlkupvm) {
                    updateOrganisaatioList.add(organisaatio);
                }
            }
        }
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
                    LOG.error("Could not update name to koodisto with organisation " + organisaatio.getOid());
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.KOODISTO, "Päivitys koodistoon epäonnistui");
                }
            } catch(ConstraintViolationException | ValidationException ve) {
                LOG.error("Validation exception with organisation " + organisaatio.getOid());
                logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.VALIDOINTI, "tiedot eivät läpäise organisaatiotarkistuksia");
                iterator.remove();
            } catch (OptimisticLockException ole) {
                LOG.error("Java persistence exception with organisation " + organisaatio.getOid(), ole.getMessage());
                iterator.remove();
                logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.TALLENNUS, "virhe tietojen tallennuksessa");
            } catch (RuntimeException re) {
                LOG.error("Could not update organisation " + organisaatio.getOid(), re.getMessage());
                iterator.remove();
                logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.TUNTEMATON, "tuntematon virhetilanne");
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

	private Boolean updateOrgAlkupvm(YTJDTO ytjdto, Organisaatio organisaatio, Boolean forceUpdate) {
        Boolean update = false;
        if(ytjdto.getYritysTunnus().getAlkupvm() == null) {
            return false;
        }
        Date ytunnusAlkupvm;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            ytunnusAlkupvm = format.parse(ytjdto.getYritysTunnus().getAlkupvm());
        } catch (ParseException pe) {
            logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "virheellinen Y-tunnuksen päivämäärä");
            LOG.error("virheellinen Y-tunnuksen päivämäärä");
            return false;
        }
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
                logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Organisaatiolta puuttuu nimi organisaatiopalvelussa");
                LOG.error("Organisation does not have a name. Invalid organisation. Not updating.");
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
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Virhe organisaation nimihistoriassa");
                    ytjErrorsDto.nimiValid = false;
                    ytjErrorsDto.nimiSvValid = false;
                    LOG.error("Organisation name history invalid with organisation " + organisaatio.getOid());
                }
            }

            // validate osoite
            // Find osoite with right language (finnish or swedish)
            Osoite osoite = findOsoiteByLangAndTypeFromYhteystiedot(ytjdto, organisaatio);
            // No matching kieli found from organisation so we will create an empty one to be fetched from YTJ.
            // (organisation language could be eg. fi/sv (dual) or en which are not in YTJ)
            if (osoite == null && ytjdto.getPostiOsoite() != null) {
                try{
                    addOsoiteForOrgWithYtjLang(ytjdto, organisaatio);
                } catch (ExceptionMessage e) {
                    // handle properly if adding failed
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.OSOITE, "oid-generointi uudelle osoitteelle epäonnistui");
                    LOG.error("Could not generate oid for osoite, skipping the field for " + organisaatio.getOid(), e);
                    ytjErrorsDto.osoiteValid = false;
                }
            }

            // validate puhelinnumero
            // Create new puhelinnumero if one does not exist
            if(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) == null && ytjdto.getPuhelin() != null) {
                try {
                    Puhelinnumero puhelinnumero = new Puhelinnumero();
                    puhelinnumero.setPuhelinnumero("123456789");
                    puhelinnumero.setTyyppi(Puhelinnumero.TYYPPI_PUHELIN);
                    puhelinnumero.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
                    puhelinnumero.setOrganisaatio(organisaatio);
                    if (ytjdto.getYrityksenKieli() != null
                            && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                        puhelinnumero.setKieli(KIELI_KOODI_SV);
                    } else {
                        puhelinnumero.setKieli(KIELI_KOODI_FI);
                    }
                    organisaatio.addYhteystieto(puhelinnumero);
                } catch (ExceptionMessage e) {
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.OSOITE, "oid-generointi uudelle puhelinnumerolle epäonnistui");
                    LOG.error("Could not generate oid for puhelinnumero, skipping the field for " + organisaatio.getOid(), e);
                    ytjErrorsDto.puhelinnumeroValid = false;
                }
            }

            // validate www
            // Create new www if one does not exist (and ytj has data)
            Www www = null;
            for(Yhteystieto yhteystieto : organisaatio.getYhteystiedot()) {
                if(yhteystieto instanceof Www) {
                    www = (Www)yhteystieto;
                    break;
                }
            }
            if(www == null && ytjdto.getWww() != null) {
                try {
                    www = new Www();
                    www.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
                    www.setOrganisaatio(organisaatio);
                    if (ytjdto.getYrityksenKieli() != null
                            && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                        www.setKieli(KIELI_KOODI_SV);
                    } else {
                        www.setKieli(KIELI_KOODI_FI);
                    }
                    organisaatio.addYhteystieto(www);
                } catch (ExceptionMessage e) {
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.WWW, "oid-generointi uudelle www-osoitteelle epäonnistui");
                    LOG.error("Could not generate oid for www, skipping the field for " + organisaatio.getOid(), e);
                    ytjErrorsDto.wwwValid = false;
                }
            }
        }
    }

    // Validates data coming from ytj so that update() does not need to worry about getting stuck on hibernate validation.
    private void validateYTJData(final Organisaatio organisaatio, final YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto, YtjPaivitysLoki loki) {
        List<YtjVirhe> validointiVirheet = new ArrayList<>();
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
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    final Date ytunnusAlkupvm = format.parse(ytjdto.getYritysTunnus().getAlkupvm());
                    final OrganisaatioMuokkausTiedotDTO tiedotDTO = new OrganisaatioMuokkausTiedotDTO() {{
                        setAlkuPvm(ytunnusAlkupvm);
                        setOid(organisaatio.getOid());
                    }};

                    businessService.batchValidatePvm(
                            new HashMap<String, OrganisaatioMuokkausTiedotDTO>() {{
                                put(tiedotDTO.getOid(), tiedotDTO);
                            }},
                            new HashMap<String, Organisaatio>() {{
                                put(organisaatio.getOid(), organisaatio);
                            }});
                } catch(ParseException | NullPointerException e) {
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "Virheellinen alkupäivämäärä");
                    LOG.error("Could not parse YTJ ytunnus alkupvm for organisation " + organisaatio.getOid());
                    ytjErrorsDto.ytunnusPvmValid = false;
                } catch (OrganisaatioDateException de) {
                    logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.ALKUPVM, "YTJ alkupäivämäärä ei läpäise tarkistuksia");
                    LOG.error("YTJ ytunnus alkupvm does not pass pvm constraints for organisation " + organisaatio.getOid());
                    ytjErrorsDto.ytunnusPvmValid = false;
                } catch (AliorganisaatioLakkautusKoulutuksiaException ke) {
                    LOG.error("YTJ ytunnus loppupvm matching organisation has koulutuksia after loppupvm for organisation ",
                            organisaatio.getOid());
                    ytjErrorsDto.ytunnusPvmValid = false;
                }
            }
        }
    }

    private void checkYtjNames(Organisaatio organisaatio, YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {
        if(ytjdto.getNimi() == null) {
            logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Nimi puuttuu YTJ:stä");
            ytjErrorsDto.nimiValid = false;
        }
        else if(ytjdto.getNimi().length() > ValidationConstants.GENERIC_MAX) {
            logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Nimen pituus YTJ:ssä ylittää maksimimerkkimäärän");
            ytjErrorsDto.nimiValid = false;
        }
        if(ytjdto.getSvNimi() == null) {
            logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Ruotsinkielinen nimi puuttuu YTJ:stä");
            ytjErrorsDto.nimiSvValid = false;
        }
        else if(ytjdto.getSvNimi().length() > ValidationConstants.GENERIC_MAX) {
            logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Ruotsinkielisen nimen pituus YTJ:ssä ylittää maksimimerkkimäärän");
            ytjErrorsDto.nimiSvValid = false;
        }
        // Allow ampersand characters
        htmlDecodeAmpInYtjNames(ytjdto);
        // In case updating from ytj would violate organisation service rule that current nimi must be the newest one,
        // we do not update nimi
        Date ytjAlkupvm = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            ytjAlkupvm = format.parse(ytjdto.getAloitusPvm());
        }
        catch(ParseException | NullPointerException e) {
            LOG.error("Could not parse YTJ date. Not updating name history.");
            ytjErrorsDto.nimiHistory = false;
        }
        // Nimi and nimihistory validation
        if(ytjAlkupvm != null) {
            checkNameHistoryForYtj(organisaatio, ytjErrorsDto, ytjAlkupvm);
        }
    }

    private void checkNameHistoryForYtj(Organisaatio organisaatio, YTJErrorsDto ytjErrorsDto, Date ytjAlkupvm) {
        for(OrganisaatioNimi organisaatioNimi : organisaatio.getNimet()) {
            // In case nimi alkupvm already exist or newer alkupvm exist, do not update name history.
            if(organisaatioNimi.getAlkuPvm().equals(ytjAlkupvm) || organisaatioNimi.getAlkuPvm().after(ytjAlkupvm)) {
                LOG.error("There is a name with same or later date in YTJ for organisation " + organisaatio.getOid());
                logYtjError(ytjPaivitysLoki, organisaatio, YtjVirhe.YTJVirheKohde.NIMI, "Organisaatiolla on nimi samalla tai uudemmalla alkupäivämäärällä kuin YTJ:ssä");
                ytjErrorsDto.nimiValid = false;
                ytjErrorsDto.nimiSvValid = false;
                ytjErrorsDto.nimiHistory = false;
            }
        }
    }

    private boolean updateWwwFromYTJToOrganisation(boolean forceUpdate, YTJDTO ytjdto, Organisaatio organisaatio) {
        boolean update = false;
        Www www = new Www();
        // Find the www from organisaatio
        for(Yhteystieto yhteystieto : organisaatio.getYhteystiedot()) {
            if(yhteystieto instanceof Www) {
                www = (Www)yhteystieto;
                break;
            }
        }
        // Update www from YTJ if it missmatches the current one.
        if((!ytjdto.getWww().equals(www.getWwwOsoite()))
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
    private void updateLangFromYTJ(YTJDTO ytjdto, Organisaatio organisaatio) {
        Boolean kieliExists = false;
        for (String kieli : organisaatio.getKielet()) {
            if (kieli.trim().equals(ORG_KIELI_KOODI_FI)
                    && ytjdto.getYrityksenKieli().trim().equals("Suomi")) {
                kieliExists = true;
            }
            if (kieli.trim().equals(ORG_KIELI_KOODI_SV)
                    && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                kieliExists = true;
            }
        }
        if (!kieliExists) {
            // XXX miksi generoidaan uusi lista eikä vaan lisätä uutta kieltä vanhalle listalle?
            String newKieli;
            List<String> newKieliList = new ArrayList<>();
            if (ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                newKieli = ORG_KIELI_KOODI_SV;
            } else {
                newKieli = ORG_KIELI_KOODI_FI;
            }
            newKieliList.addAll(organisaatio.getKielet());
            newKieliList.add(newKieli);
            organisaatio.setKielet(newKieliList);
        }
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

    private Osoite addOsoiteForOrgWithYtjLang(YTJDTO ytjdto, Organisaatio organisaatio) throws ExceptionMessage {
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
            Date ytjAlkupvm = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                ytjAlkupvm = format.parse(ytjdto.getAloitusPvm());
            }
            catch(ParseException | NullPointerException e) {
                LOG.error("Could not parse YTJ date.");
            }

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

    private void logYtjError(YtjPaivitysLoki ytjPaivitysLoki, Organisaatio organisaatio, YtjVirhe.YTJVirheKohde kohde, String viesti) {
        ytjPaivitysLoki.setPaivitysTila(YtjPaivitysLoki.YTJPaivitysStatus.ONNISTUNUT_VIRHEITA);
        YtjVirhe virhe = new YtjVirhe();
        virhe.setOid(organisaatio.getOid());
        virhe.setOrgNimi(organisaatio.getNimihaku());
        virhe.setVirhekohde(kohde);
        virhe.setVirheviesti(viesti);
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

    private void fetchDataFromYtj(List<String> ytunnusList, List<YTJDTO> ytjdtoList) {
        for (int i = 0; i < ytunnusList.size(); i += PARTITION_SIZE) {
            try {
                // Fetch data from ytj for these organisations
                ytjdtoList.addAll(ytjResource.doYtjMassSearch(ytunnusList.subList(i, Math.min(i + PARTITION_SIZE, ytunnusList.size()))));
            } catch (OrganisaatioResourceException ore) {
                LOG.error("Could not fetch ytj data. Aborting ytj data update.", ore);
                throw ore;
            }
        }
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

}
