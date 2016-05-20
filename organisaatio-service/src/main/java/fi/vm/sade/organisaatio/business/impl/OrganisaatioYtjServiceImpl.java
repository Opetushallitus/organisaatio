package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioKoodistoException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNameHistoryNotValidException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.resource.IndexerResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.YTJResource;
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

    public static final String POSTIOSOITE_PREFIX = "posti_";
    public static final String KIELI_KOODI_FI = "kieli_fi#1";
    public static final String KIELI_KOODI_SV = "kieli_sv#1";
    public static final String ORG_KIELI_KOODI_FI = "oppilaitoksenopetuskieli_1#1";
    public static final String ORG_KIELI_KOODI_SV = "oppilaitoksenopetuskieli_2#1";

    // Updates nimi and other info for all Koulutustoimija, Muu_organisaatio and Tyoelamajarjesto organisations using YTJ api
    @Override
    public List<Organisaatio> updateYTJData(final boolean forceUpdate) throws OrganisaatioResourceException {
        // Create y-tunnus list of updateable arganisations
        List<String> oidList = new ArrayList<>();
        List<String> ytunnusList = new ArrayList<>();
        List<Organisaatio> organisaatioList;
        List<Organisaatio> updateOrganisaatioList = new ArrayList<>();
        List<YTJDTO> ytjdtoList;
        int searchLimit = 10000;
        // Search the organisations using the DAO since it provides osoites.
        // Criteria: (koulutustoimija, tyoelamajarjesto, muu_organisaatio, ei lakkautettu, has y-tunnus)
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.KOULUTUSTOIMIJA));
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.TYOELAMAJARJESTO));
        oidList.addAll(organisaatioDAO.findOidsBy(true, searchLimit, 0, OrganisaatioTyyppi.MUU_ORGANISAATIO));
        if(oidList.isEmpty()) {
            LOG.debug("oidList is empty, no organisations updated from YTJ!");
            // TODO exception, update failed
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

        try {
            // Fetch data from ytj for these organisations
            ytjdtoList = ytjResource.findByYTunnusBatch(ytunnusList);
        } catch(OrganisaatioResourceException ore) {
            LOG.error("Could not fetch ytj data. Aborting ytj data update.", ore);
            // TODO add info for UI to fetch
            throw ore;
        }

        Map<String,Organisaatio> organisaatioMap = new HashMap<String,Organisaatio>();
        for (Organisaatio o : organisaatioList) {
            organisaatioMap.put(o.getYtunnus().trim(),o);
        }
        // Check which organisations need to be updated. YtjPaivitysPvm is the date when info is fetched from YTJ.
        for (YTJDTO ytjdto : ytjdtoList) {
            Organisaatio organisaatio = organisaatioMap.get(ytjdto.getYtunnus().trim());

            // TODO some basic validation first (null checks, corner cases etc)
            // don't proceed to update if there's something wrong
            // collect info to some map structure
            if (organisaatio != null) {
                Boolean updateNimi = false;
                Boolean updateOsoite = false;
                Boolean updatePuhelin = false;
                Boolean updateWww = false;
                // Update nimi
                // Allow ampersand characters
                htmlDecodeAmpInNames(ytjdto);

                // There should always exist at least one nimi.
                if (organisaatio.getNimi() == null) {
                    LOG.warn("Organisation does not have a name. Invalid organisation. Not updating.");
                    // TODO move this to the beginning
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
                        // TODO move validation checks to the beginning
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
                        // TODO move validation checks to the beginning
                        continue;
                    }
                }

                // Update www
                if(ytjdto.getWww() != null) {
                    try {
                        updateWww = updateWwwFromYTJToOrganisation(forceUpdate, ytjdto, organisaatio);
                    } catch (ExceptionMessage e) {
                        LOG.error("Could not generate oid for Www, skipping organisation", e);
                        // TODO move validation checks to the beginning
                        continue;
                    }
                }

                if (updateNimi || updateOsoite || updatePuhelin || updateWww) {
                    // Add new kieli to the organisation if there isn't one matching the YTJ kieli
                    updateLangFromYTJ(ytjdto, organisaatio);
                    updateOrganisaatioList.add(organisaatio);
                }
            }
        }

        // Update listed organisations to db and koodisto service.
        for(Organisaatio organisaatio : updateOrganisaatioList) {
            try {
                // TODO move name history validation checks to the beginning and handle properly
                checker.checkNimihistoriaAlkupvm(organisaatio.getNimet());
                organisaatioDAO.update(organisaatio);
                // update koodisto (When name has changed) TODO: call only when name actually changes.
                // Update only nimi if changed. organisaatio.paivityspvm should not have be changed here.
                // TODO it would be good to get an exception from the koodisto client if anything fails
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

    // Update nimi to Organisaatio from YTJ and handle the name history (nimet).
    // TODO refactor to create new name and change references everywhere to point to this one leaving old as history entry
    // TODO instead of editing the current one and creating new history entry.
    private void updateNamesFromYTJ(YTJDTO ytjdto, final Organisaatio organisaatio) {
        if (organisaatio.getNimi().getString("fi") != null || organisaatio.getNimi().getString("sv") != null) {
            // If organisaatio (faultly) has no nimihistoria but organisaatio still has nimi create new entry
            // containing current information.
            if(organisaatio.getNimet().isEmpty()) {
                organisaatio.addNimi(new OrganisaatioNimi(){{
                    setOrganisaatio(organisaatio);
                    setNimi(organisaatio.getNimi());
                    setAlkuPvm(organisaatio.getAlkuPvm());
                }});
            }
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
                        OrganisaatioNimi newOrgNimi = createOrganisaatioNimiWithYTJLang(ytjdto, orgNimi);
                        organisaatio.addNimi(newOrgNimi);
                    }
                    // When updating nimi always update alkupvm from YTJ as toiminimen alkupvm.
                    try {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        orgNimi.setAlkuPvm(format.parse(ytjdto.getAloitusPvm()));
                    }
                    catch(ParseException | NullPointerException e) {
                        LOG.error("Could not parse YTJ date. Using the old date.", e);
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

    private void htmlDecodeAmpInNames(YTJDTO ytjdto) {
        // TODO this would be better to fix in validator (or get rid of html encoding in the backend)
        if(ytjdto.getNimi() != null) {
            ytjdto.setNimi(ytjdto.getNimi().replace("&amp;", "&"));
        }
        if(ytjdto.getSvNimi() != null) {
            ytjdto.setSvNimi(ytjdto.getSvNimi().replace("&amp;", "&"));
        }
    }


}
