package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.generic.common.validation.ValidationConstants;
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Transactional
@Service("organisaatioYtjService")
public class OrganisaatioYtjServiceImpl implements OrganisaatioYtjService {

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

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

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private static final String POSTIOSOITE_PREFIX = "posti_";
    private static final String KIELI_KOODI_FI = "kieli_fi#1";
    private static final String KIELI_KOODI_SV = "kieli_sv#1";
    private static final String ORG_KIELI_KOODI_FI = "oppilaitoksenopetuskieli_1#1";
    private static final String ORG_KIELI_KOODI_SV = "oppilaitoksenopetuskieli_2#1";
    private static final int SEARCH_LIMIT = 10000;
    private static final int PARTITION_SIZE = 1000;

    // Updates nimi and other info for all Koulutustoimija, Muu_organisaatio and Tyoelamajarjesto organisations using YTJ api
    @Override
    public List<Organisaatio> updateYTJData(final boolean forceUpdate) throws OrganisaatioResourceException{
        // Create y-tunnus list of updateable arganisations
        List<String> oidList = new ArrayList<>();
        List<Organisaatio> organisaatioList;
        List<String> ytunnusList = new ArrayList<>();
        List<YTJDTO> ytjdtoList = new ArrayList<>();
        Map<String,Organisaatio> organisaatioMap = new HashMap<>();
        List<Organisaatio> updateOrganisaatioList = new ArrayList<>();
        // Search the organisations using the DAO since it provides osoites.
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, SEARCH_LIMIT, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        if(oidList.isEmpty()) {
            LOG.debug("oidList is empty, no organisations updated from YTJ!");
            // TODO exception, update failed
            return updateOrganisaatioList;
        }
        organisaatioList = organisaatioDAO.findByOidList(oidList, SEARCH_LIMIT);
        // Fill the Y-tunnus list and parse off organisaatios that are lakkautettu
        for(Organisaatio organisaatio : organisaatioList) {
            if(organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.AKTIIVINEN
                    || organisaatio.getStatus() == Organisaatio.OrganisaatioStatus.SUUNNITELTU) {
                ytunnusList.add(organisaatio.getYtunnus());
                organisaatioMap.put(organisaatio.getYtunnus().trim(), organisaatio);
            }
        }

        fetchDataFromYtj(ytunnusList, ytjdtoList);

        // Check which organisations need to be updated. YtjPaivitysPvm is the date when info is fetched from YTJ.
        for (YTJDTO ytjdto : ytjdtoList) {
            Organisaatio organisaatio = organisaatioMap.get(ytjdto.getYtunnus().trim());
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
                // Update nimi
                if (ytjErrorsDto.nimiValid  || ytjErrorsDto.nimiSvValid) {
                    updateNimi = updateNameFromYTJ(ytjdto, organisaatio, forceUpdate);
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

                if (updateNimi || updateOsoite || updatePuhelin || updateWww) {
                    updateOrganisaatioList.add(organisaatio);
                }
            }
        }

        // Update listed organisations to db and koodisto service.
        for(Organisaatio organisaatio : updateOrganisaatioList) {
            try {
                organisaatioDAO.updateOrg(organisaatio);
                // update koodisto (When name has changed) TODO: call only when name actually changes.
                // Update only nimi if changed. organisaatio.paivityspvm should not have be changed here.
                if(organisaatioKoodisto.paivitaKoodisto(organisaatio, false) != null) {
                    LOG.error("Could not update name to koodisto with organisation " + organisaatio.getOid());
                    // TODO log the failure to errordto
                }
            } catch (OptimisticLockException ole) {
                LOG.error("Java persistence exception with organisation " + organisaatio.getOid(), ole.getMessage());
                // TODO log the failure to errordto
            } catch (RuntimeException re) {
                LOG.error("Could not update organisation " + organisaatio.getOid(), re.getMessage());
                // TODO log the failure to errordto
            }
        }
        // Index the updated resources.
        solrIndexer.index(updateOrganisaatioList);

        return updateOrganisaatioList;
    }

    private void fetchDataFromYtj(List<String> ytunnusList, List<YTJDTO> ytjdtoList) {
        for(int i = 0; i< ytunnusList.size(); i+= PARTITION_SIZE) {
            try {
                // Fetch data from ytj for these organisations
                ytjdtoList.addAll(ytjResource.doYtjMassSearch(ytunnusList.subList(i, Math.min(i + PARTITION_SIZE, ytunnusList.size()))));
            } catch(OrganisaatioResourceException ore) {
                LOG.error("Could not fetch ytj data. Aborting ytj data update.", ore);
                // TODO add info for UI to fetch
                throw ore;
            }
        }
    }

    private void validateOrganisaatioDataForYTJ(final Organisaatio organisaatio, YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {
        // There should always exist at least one nimi.
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
                LOG.error("Organisation does not have a name. Invalid organisation. Not updating.");
            }
            // If organisaatio (faultly) has no nimihistoria but organisaatio still has nimi create new entry
            // containing current information.
            else if(organisaatio.getNimet().isEmpty()) {
                organisaatio.addNimi(new OrganisaatioNimi(){{
                    setOrganisaatio(organisaatio);
                    setNimi(organisaatio.getNimi());
                    setAlkuPvm(organisaatio.getAlkuPvm());
                }});
            }
            else {
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
                    // TODO handle if bad
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
                    LOG.error("Could not generate oid for osoite, skipping the field for " + organisaatio.getOid(), e);
                    ytjErrorsDto.osoiteValid = false;
                }
            }

            // validate puhelinnumero
            // Create new puhelinnumero if one does not exist
            if(organisaatio.getPuhelin(Puhelinnumero.TYYPPI_PUHELIN) == null && ytjdto.getPuhelin() != null) {
                try {
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
                } catch (ExceptionMessage e) {
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
                    www = new Www(oidService.newOid(NodeClassCode.TEKN_5));
                    www.setOrganisaatio(organisaatio);
                    if (ytjdto.getYrityksenKieli() != null
                            && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                        www.setKieli(KIELI_KOODI_SV);
                    } else {
                        www.setKieli(KIELI_KOODI_FI);
                    }
                    organisaatio.addYhteystieto(www);
                } catch (ExceptionMessage e) {
                    LOG.error("Could not generate oid for www, skipping the field for " + organisaatio.getOid(), e);
                    ytjErrorsDto.wwwValid = false;
                }
            }
        }
    }

    // Validates data coming from ytj so that update() does not need to worry about getting stuck on hibernate validation.
    private void validateYTJData(Organisaatio organisaatio, YTJDTO ytjdto, YTJErrorsDto ytjErrorsDto) {
        if(ytjdto == null) {
            ytjErrorsDto.organisaatioValid = false;
        }
        else {
            // nimi
            if(ytjdto.getNimi() == null) {
                ytjErrorsDto.nimiValid = false;
            }
            else if(ytjdto.getNimi().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.nimiValid = false;
            }
            if(ytjdto.getSvNimi() == null) {
                ytjErrorsDto.nimiSvValid = false;
            }
            else if(ytjdto.getSvNimi().length() > ValidationConstants.GENERIC_MAX) {
                ytjErrorsDto.nimiSvValid = false;
            }
            // Allow ampersand characters
            htmlDecodeAmpInYtjNames(ytjdto);
            // In case updating from ytj would violate organisation service rule that current nimi must be the newest one,
            // we do not update nimi
            if(ytjdto.getAloitusPvm() != null) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    Date ytjAlkupvm = format.parse(ytjdto.getAloitusPvm());
                    for(OrganisaatioNimi organisaatioNimi : organisaatio.getNimet()) {
                        if(organisaatioNimi.getAlkuPvm().after(ytjAlkupvm)) {
                            ytjErrorsDto.nimiValid = false;
                            ytjErrorsDto.nimiSvValid = false;
                            // TODO gather error data
                            break;
                        }
                    }
                }
                catch(ParseException | NullPointerException e) {
                    LOG.error("Could not parse YTJ date.", e);
                }
            }

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
                break;
            }
            if (kieli.trim().equals(ORG_KIELI_KOODI_SV)
                    && ytjdto.getYrityksenKieli().trim().equals(YtjDtoMapperHelper.KIELI_SV)) {
                kieliExists = true;
                break;
            }
        }
        if (!kieliExists) {
            String newKieli;
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

    // Update nimi to Organisaatio from YTJ and handle the name history (nimet). Does not require nimi.get(lang) to exist.
    // TODO refactor to create new name and change references everywhere to point to this one leaving old as history entry
    // TODO instead of editing the current one and creating new history entry.
    private boolean updateNameFromYTJ(YTJDTO ytjdto, final Organisaatio organisaatio, final boolean forceUpdate) {
        boolean update = false;
        if((ytjdto.getNimi() != null && !ytjdto.getNimi().equals(organisaatio.getNimi().getString("fi")))
                || (ytjdto.getSvNimi() != null && !ytjdto.getSvNimi().equals(organisaatio.getNimi().getString("sv")))
                || ((ytjdto.getNimi() != null || ytjdto.getSvNimi() != null) && forceUpdate)) {
            Date ytjAlkupvm = null;
            OrganisaatioNimi currentOrgNimi = null;
            if(organisaatio.getNimi().getString("fi") != null || organisaatio.getNimi().getString("sv") != null) {
                boolean updateNimiHistory = true;
                try {
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    ytjAlkupvm = format.parse(ytjdto.getAloitusPvm());
                }
                catch(ParseException | NullPointerException e) {
                    updateNimiHistory = false;
                    LOG.error("Could not parse YTJ date.", e);
                }
                // In case nimi alkupvm already exist or ytj has no alkupvm, do not update name history.
                for(OrganisaatioNimi orgNimi : organisaatio.getNimet()) {
                    if(ytjdto.getAloitusPvm() == null || orgNimi.getAlkuPvm().equals(ytjAlkupvm)) {
                        updateNimiHistory = false;
                        break;
                    }
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
            if(ytjAlkupvm != null && currentOrgNimi != null) {
                currentOrgNimi.setAlkuPvm(ytjAlkupvm);
            }
            update = true;
        }
        return update;
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
