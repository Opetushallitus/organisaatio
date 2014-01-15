package fi.vm.sade.organisaatio.resource;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisByKoodistoCriteriaType;
import fi.vm.sade.koodisto.service.types.SearchKoodistosCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodiUriAndVersioType;
import fi.vm.sade.koodisto.service.types.common.KoodistoType;
import fi.vm.sade.koodisto.service.types.common.SuhteenTyyppiType;
import fi.vm.sade.koodisto.util.KoodiServiceSearchCriteriaBuilder;
import fi.vm.sade.koodisto.util.KoodistoServiceSearchCriteriaBuilder;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;

import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import fi.vm.sade.oid.service.ExceptionMessage;
import fi.vm.sade.oid.service.OIDService;
import fi.vm.sade.oid.service.types.NodeClassCode;
import fi.vm.sade.organisaatio.api.OrganisaatioValidationConstraints;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dao.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.Yhteystieto;
import fi.vm.sade.organisaatio.model.lop.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.service.LearningInstitutionExistsException;
import fi.vm.sade.organisaatio.service.OrganisaatioHierarchyException;
import fi.vm.sade.organisaatio.service.OrganisationDateValidator;
import fi.vm.sade.organisaatio.service.OrganisationHierarchyValidator;
import fi.vm.sade.organisaatio.service.OrganizationDateException;
import fi.vm.sade.organisaatio.service.YtunnusException;
import fi.vm.sade.organisaatio.service.auth.PermissionChecker;
import fi.vm.sade.organisaatio.service.search.OrganisaatioSearchService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;

import org.apache.cxf.jaxrs.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Antti Salonen
 * @author mlyly
 */
@Component
@Transactional(readOnly = true)
@CrossOriginResourceSharing(allowAllOrigins = true)
@PreAuthorize("isAuthenticated()")
public class OrganisaatioResourceImpl implements OrganisaatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImpl.class);
    @Autowired
    private OrganisaatioSearchService organisaatioSearchService;
    @Autowired
    private OrganisaatioDAOImpl organisaatioDAO;
    @Autowired
    private OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;
    @Autowired
    private ConversionService conversionService;
    @Autowired
    private IndexerResource solrIndexer;
    @Autowired
    private KoodiService koodiService;
    @Autowired
    private KoodistoService koodistoService;
    @Autowired
    private OIDService oidService;
    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Value("${koodisto-uris.opetuspisteet}")
    private String toimipistekoodisto;

    @Value("${koodisto-uris.yhteishaunkoulukoodi}")
    private String yhKoulukoodiKoodisto;

    @Autowired
    PermissionChecker permissionChecker;

    private static final String parentSeparator = "|";
    private static final String parentSplitter = "\\|";

    @Override
    public OrganisaatioHakutulos searchBasic(OrganisaatioSearchCriteria s) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        if(s.getOppilaitosTyyppi()!=null && s.getOppilaitosTyyppi().length()==0) {
            s.setOppilaitosTyyppi(null);
        }

        if(s.getOrganisaatioTyyppi()!=null && s.getOrganisaatioTyyppi().length()==0) {
            s.setOrganisaatioTyyppi(null);
        }

//        System.out.println("oidRestrictionList:" + s.getOidRestrictionList());

        List<OrganisaatioPerustieto> organisaatiot = organisaatioSearchService.searchBasicOrganisaatios(s);

        //sorttaa
        final Ordering<OrganisaatioPerustieto> ordering = Ordering.natural().nullsFirst().onResultOf(new Function<OrganisaatioPerustieto, Comparable<String>>() {
            public Comparable<String> apply(OrganisaatioPerustieto input) {
                return OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), input);

            };
        });

        organisaatiot = ordering.immutableSortedCopy(organisaatiot);

        //rakenna hierarkia
        tulos.setOrganisaatiot(createHierarchy(organisaatiot));

        tulos.setNumHits(organisaatiot.size());
        return tulos;
    }




    /**
     * Luo puumaisen organisaatiohierarkian. palauttaa listan juuritason
     * organisaatioista ja asettaa organisaatioille lapset.
     */
    private List<OrganisaatioPerustieto> createHierarchy(
            final List<OrganisaatioPerustieto> organisaatiot) {

        Map<String, OrganisaatioPerustieto> oidToOrgMap = new HashMap<String, OrganisaatioPerustieto>();

        //ORganisaatiot joilla eil ole isää:
        List<OrganisaatioPerustieto> rootOrgs = new ArrayList<OrganisaatioPerustieto>();

        for (OrganisaatioPerustieto curOrg : organisaatiot) {
            oidToOrgMap.put(curOrg.getOid(), curOrg);
        }

        for (OrganisaatioPerustieto curOrg : organisaatiot) {
            final String parentOid = curOrg.getParentOid();
            final OrganisaatioPerustieto parentOrg = oidToOrgMap.get(parentOid);
            if (parentOrg != null) {
                parentOrg.getChildren().add(curOrg);
            } else {
                rootOrgs.add(curOrg);
            }
        }

        return rootOrgs;

    }

    // GET /organisaatio/{oid}/children
    @Override
    public List<OrganisaatioRDTO> children(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioDAO.findByOid(oid);
        List<OrganisaatioRDTO> childList = new LinkedList<OrganisaatioRDTO>();
        if (parentOrg != null) {
            List<OrganisaatioSuhde> suhteet = parentOrg.getChildSuhteet();
            for (OrganisaatioSuhde suhde : suhteet) {
                childList.add(conversionService.convert(suhde.getChild(), OrganisaatioRDTO.class));
            }
        }
        return childList;
    }

    // GET /organisaatio/{oid}/childoids
    @Override
    public String childoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioDAO.findByOid(oid);
        List<String> childOidList = new LinkedList<String>();
        if (parentOrg != null) {
            List<OrganisaatioSuhde> suhteet = parentOrg.getChildSuhteet();
            for (OrganisaatioSuhde suhde : suhteet) {
                childOidList.add("\"" + suhde.getChild().getOid() + "\"");
            }
        }
        return "{ \"oids\": [" + Joiner.on(",").join(childOidList).toString() + "]}";
    }

    // GET /organisaatio/{oid}/parentoids - used for security purposes
    @Override
    public String parentoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        // find parents
        final List<String> parentOidList = organisaatioSearchService.findParentOids(oid);
        Collections.reverse(parentOidList);

        // NOTE - this assumes everything is under one "root", ie. "OPH"
        if (!parentOidList.contains(rootOrganisaatioOid)) {
            parentOidList.add(0, rootOrganisaatioOid); // add root organisaatio if needed
        }
        if (!parentOidList.contains(oid)) {
            parentOidList.add(oid); // add self if needed
        }
        return Joiner.on(OID_SEPARATOR).join(parentOidList).toString();
    }

    @Override
    public String hello() {
        return "Well Hello! " + new Date();
    }

    // GET /organisaatio?searchTerms=x&count=10&startIndex=100&lastModifiedBefore=X&lastModifiedSince=Y
    @Override
    public List<String> search(String searchTerms, int count, int startIndex, Date lastModifiedBefore, Date lastModifiedSince) {
        LOG.debug("search({}, {}, {}, {}, {})", new Object[]{searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince});

        // Check the type spesified search
        OrganisaatioTyyppi type = null;
        if (searchTerms != null) {
            for (OrganisaatioTyyppi organisaatioTyyppi : OrganisaatioTyyppi.values()) {
                if (searchTerms.contains("type=" + organisaatioTyyppi.name())) {
                    type = organisaatioTyyppi;
                    break;
                }
            }
        }

        List<String> result = organisaatioDAO.findOidsBy(searchTerms, count, startIndex, lastModifiedBefore, lastModifiedSince, type);
        LOG.debug("  result.size = {}", result.size());
        return result;
    }

    // GET /organisaatio/{oid}
    @Override
    public OrganisaatioRDTO getOrganisaatioByOID(final String oid) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);


        // Search order
        // 1. OID
        // 2. Y-TUNNUS
        // 3. VIRASTOTUNNUS
        // 4. OPPILAITOSKOODI
        // 5. TOIMIPISTEKOODI
        Organisaatio o = organisaatioDAO.findByOid(oid);
        if (o == null) {
            o = organisaatioDAO.findByYTunnus(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByVirastoTunnus(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByOppilaitoskoodi(oid);
        }
        if (o == null) {
            o = organisaatioDAO.findByToimipistekoodi(oid);
        }
        //If the organisaatio is opetuspiste (toimipiste) and it does not have a value in yhteishaunKoulukoodi field
        //such is saught from koodisto
        if (o.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE.value()) && isEmpty(o.getYhteishaunKoulukoodi())) {
            updateYhKoulukoodi(o);
        }

        OrganisaatioRDTO result = conversionService.convert(o, OrganisaatioRDTO.class);

        LOG.debug("  result={}", result);
        return result;
    }

    /*
     * method that searches for the yhkoodi of the organisaatio. If such is found is updated
     * to the yhteyishanKoulukoodi field of organisaatio.
     */
    private void updateYhKoulukoodi(Organisaatio o) {
        String yhKoodi = null;
        String olkoodi = null;
        Organisaatio parentOl = null;
        if (o.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            olkoodi = o.getOppilaitosKoodi();
        } else if ((parentOl = getParentOl(o)) != null){
            olkoodi = parentOl.getOppilaitosKoodi();
        }
        if (!isEmpty(olkoodi)) {
            yhKoodi = getYhkoodi(olkoodi, this.getOpPisteenJarjNro(o));
        }
        if (!isEmpty(yhKoodi)) {
            o.setYhteishaunKoulukoodi(yhKoodi);
            this.organisaatioDAO.update(o);
        }
    }

    /*
     * Method that seeks the nearest oppilaitos ancestor of organisaatio o.
     */
    private Organisaatio getParentOl(Organisaatio o) {
        Organisaatio parentOl = o.getParent();
        if (parentOl == null || parentOl.getOid().equals(this.rootOrganisaatioOid)) {
            return null;
        }
        if (parentOl.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            return parentOl;
        }
        return getParentOl(parentOl);
    }

    private boolean isEmpty(String val) {
        return val == null || val.isEmpty();
    }

    /*
     * method that seeks the yhkoodi for organisaatio in koodisto. Organisaatio is represented by olkoodi and opJnro.
     * Organisaatio - > Organisaatiokoodi (in: opetuspisteet -koodisto) -> yhkoodi (in: yhteishaunkoulukoodisto)
     */
    private String getYhkoodi(String olkoodi, String opJnro) {
        List<KoodiType> koodis = getKoodisByArvoAndKoodisto(olkoodi + opJnro, toimipistekoodisto);
        KoodiType opNroKoodi = null;
        KoodiType yhKoodi = null;
        if (koodis != null && !koodis.isEmpty()) {
            opNroKoodi = koodis.get(0);
            yhKoodi = getSisaltyvaKoodi(opNroKoodi, yhKoulukoodiKoodisto);
        }
        return (yhKoodi == null) ? null : yhKoodi.getKoodiArvo();
    }

    /*
     * Gets the opetuspisteenJarjNro for organisaatio.
     */
    private String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return opPisteenJarjNro;
    }

    /*
     * Search for koodis by arvo and koodisto.
     */
    private List<KoodiType> getKoodisByArvoAndKoodisto(String arvo, String koodistoUri) {
        try {
            SearchKoodistosCriteriaType koodistoSearchCriteria = KoodistoServiceSearchCriteriaBuilder.latestKoodistoByUri(koodistoUri);

            List<KoodistoType> koodistoResult = koodistoService.searchKoodistos(koodistoSearchCriteria);
            if(koodistoResult.size() != 1) {
                // FIXME: Throw something other than RuntimeException?
                throw new RuntimeException("No koodisto found for koodisto URI " + koodistoUri);
            }
            KoodistoType koodisto = koodistoResult.get(0);

            SearchKoodisByKoodistoCriteriaType koodiSearchCriteria = KoodiServiceSearchCriteriaBuilder.koodisByArvoAndKoodistoUriAndKoodistoVersio(arvo,
                    koodistoUri, koodisto.getVersio());
            return koodiService.searchKoodisByKoodisto(koodiSearchCriteria);
        } catch (Exception exp) {
            exp.printStackTrace();
            return null;
        }
    }

    /*
     * Getting a sisaltyva koodi.
     */
    private KoodiType getSisaltyvaKoodi(KoodiType sourcekoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(sourcekoodi.getKoodiUri());
        uriAndVersio.setVersio(sourcekoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.SISALTYY);
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                return curKoodi;
            }
        }
        return null;
    }



    @Override
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    public OrganisaatioRDTO updateOrganisaatio(String oid, OrganisaatioRDTO ordto) {
        permissionChecker.checkSaveOrganisation(ordto, true);
        Organisaatio savedOrg = save(ordto, true, true);
        OrganisaatioRDTO ret = conversionService.convert(savedOrg, OrganisaatioRDTO.class);
        return ret;
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
        } else {
            return false;
        }
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

    private void getDescendantSuhteet(Organisaatio parentE,
                                      List<OrganisaatioSuhde> children) {
        if (parentE == null) {
            return;
        }
        List<OrganisaatioSuhde> curChildren = this.organisaatioSuhdeDAO.findBy("parent", parentE);
        if (curChildren != null) {
            for (OrganisaatioSuhde curChildSuhde : curChildren) {
                getDescendantSuhteet(curChildSuhde.getChild(), children);
            }
            children.addAll(curChildren);
        }

    }

    /*
     * Generating the opetuspiteenJarjNro for an opetuspiste. The opetuspiteenJarjNro is the count of the cescendants of the parent oppilaitos
     * + 1.
     */
    private String generateOpetuspisteenJarjNro(Organisaatio entity,
                                                OrganisaatioRDTO model) {
        //Opetuspisteen jarjestysnumero is only generated to opetuspiste which is not also an oppilaitos
        if (model.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE.value()) && !model.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            Organisaatio oppilaitosE = findClosestOppilaitos(entity);

            List<OrganisaatioSuhde> children = new ArrayList<OrganisaatioSuhde>();
            getDescendantSuhteet(oppilaitosE, children);
            int nextVal = children.size() + 1;

            String jarjNro = (nextVal < 10) ? String.format("%s%s", "0", nextVal) : String.format("%s", nextVal);
            entity.setOpetuspisteenJarjNro(jarjNro);
            return jarjNro;
        }
        return null;
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
            if (organisaatio.getId() != null) { // we can haz children only if we are already saved
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
     * Simple recursive operuspiste / toimipiste koodi calculation.
     *
     * Search "up" for OPPILAITOS and return it's OppilaitosKoodi and then append OPETUSPISTE order number(s).
     *
     * @param s
     * @return
     */
    private String calculateAndUpdateToimipisteKoodi2(Organisaatio s) {
        LOG.info("calculateAndUpdateToimipisteKoodi2(org={})", s);

        if (s == null) {
            LOG.info("  org  == null, return ''");
            return "";
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.OPPILAITOS)) {
            LOG.info("  org  == OPPILAITOS, return oppilaitoskoodi: '{}'", s.getOppilaitosKoodi());
            return s.getOppilaitosKoodi();
        }

        if (organisaatioIsOfType(s, OrganisaatioTyyppi.OPETUSPISTE)) {
            LOG.info("  org  == OPETUSPISTE, return parent opk/olk code AND this ones order number: '{}'", s.getOpetuspisteenJarjNro());
            String onum = isEmpty(s.getOpetuspisteenJarjNro()) ? "01" : s.getOpetuspisteenJarjNro();
            Organisaatio parent = null;
            if (s.getId() != null) {
                parent = (s.getParent() != null) ? s.getParent() : this.organisaatioSuhdeDAO.findParentTo(s.getId(), new Date()).getParent();
            } else {
                parent = s.getParent();
            }
            return calculateAndUpdateToimipisteKoodi2(parent) + onum;
        }

        LOG.error("calculateAndUpdateToimipisteKoodi2 == TYPE unknown?: types='{}'", s.getTyypit());

        return "";
    }

    private Organisaatio saveParentSuhde(Organisaatio child, Organisaatio parent, String opJarjNro) {
        OrganisaatioSuhde curSuhde = organisaatioSuhdeDAO.findParentTo(child.getId(), null);
        if (parent != null && (curSuhde == null || curSuhde.getParent().getId() != parent.getId())) {
            curSuhde = organisaatioSuhdeDAO.addChild(parent.getId(), child.getId(), Calendar.getInstance().getTime(), opJarjNro);
        }
        child.setParentSuhteet(organisaatioSuhdeDAO.findBy("child", child));
        return this.organisaatioDAO.findByOid(child.getOid());
    }

    private Organisaatio save(OrganisaatioRDTO model, boolean updating, boolean skipParentDateValidation) throws ValidationException {
        //Tarkistetaan OID
        if (model.getOid() == null && updating) {
            throw new ValidationException("Oid cannot be null");//trying to update organisaatio that doesn't exist (is is null)");
        } else if (!updating) {
            if ((model.getOid() != null) && (organisaatioDAO.findByOid(model.getOid()) != null)) {
                throw new RuntimeException("organisaatio already exists, cannot re-create, use update instead");
            }

            if (model.getOppilaitosKoodi() != null && model.getOppilaitosKoodi().length() > 0) {
                if (checkLearningInstitutionCodeIsUniqueAndNotUsed(model)) {
                    throw new LearningInstitutionExistsException("oppilaitos.exists.with.code");
                }
            }
        }
        // validointi: y-tunnus
        if (model.getYTunnus() != null && model.getYTunnus().length() == 0) {
            model.setYTunnus(null);
        }
        if (model.getYTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.YTUNNUS_PATTERN, model.getYTunnus())) {
            throw new ValidationException("validation.Organisaatio.ytunnus");
        }

        // validointi: virastotunnus
        if (model.getVirastoTunnus() != null && model.getVirastoTunnus().length() == 0) {
            model.setVirastoTunnus(null);
        }
        if (model.getVirastoTunnus() != null && !Pattern.matches(OrganisaatioValidationConstraints.VIRASTOTUNNUS_PATTERN, model.getVirastoTunnus())) {
            throw new ValidationException("validation.Organisaatio.virastotunnus");
        }

        //Setting the parent paths
        Organisaatio entity = conversionService.convert(model, Organisaatio.class); //this entity is populated with new data
        if (updating) {
            Organisaatio orgEntity = this.organisaatioDAO.findByOid(model.getOid());
            entity.setId(orgEntity.getId());
        }
        try {
            generateOids(entity);
            generateOidsMetadata(entity.getMetadata());
        }
        catch (ExceptionMessage em) {
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR, em.getMessage());
        }
        createParentPath(entity, model.getParentOid());

        //Generating the opetuspiteenJarjNro
        String opJarjNro = null;
        if (!updating && StringUtils.isEmpty(model.getOpetuspisteenJarjNro())) {
            opJarjNro = generateOpetuspisteenJarjNro(entity, model);
        } else {
            opJarjNro = model.getOpetuspisteenJarjNro();
        }

        //If inserting, check if ytunnus allready exists in the database
        if (!updating && entity.getYtunnus() != null) {
            checkYtunnusIsUniqueAndNotUsed(entity.getYtunnus());
        }

        entity.setOrganisaatioPoistettu(false);
        //Check if organization has parent and if it has check that passivation dates match to parent
        Organisaatio parentOrg = (model.getParentOid() != null && !model.getParentOid().equalsIgnoreCase(rootOrganisaatioOid))
                ? organisaatioDAO.findByOid(model.getParentOid()) : null;



        //OVT-4765 do not validate start date against parent date when updating
        if (updating) {
            LOG.info("this is an update, not validating parent dates.");
            skipParentDateValidation = true;
        }
        //OH-116
        if (parentOrg != null) {
            OrganisationDateValidator dateValidator = new OrganisationDateValidator(skipParentDateValidation);
            if (!dateValidator.apply(Maps.immutableEntry(parentOrg, entity))) {
                throw new OrganizationDateException();
            }
        }

        // Validoidaan organisaation lisätiedot
        List<String> orgTypes = new ArrayList<String>();
        String tyypitStr = "";
        for (String curTyyppi : model.getTyypit()) {
            orgTypes.add(curTyyppi);
            tyypitStr += curTyyppi + "|";
        }
        entity.setOrganisaatiotyypitStr(tyypitStr);

        // TODO
        //validateLisatiedot(model.getYhteystietoArvos(), orgTypes);



        // filtteröi organisaatiotyypin mukaan
        //entity.setYhteystietoArvos(mergeYhteystietoArvos(entity, converterFactory.convertYhteystietoArvosToJPA(model.getYhteystietoArvos(), YhteystietoArvo.class, true)));


        // Tarkistetaan organisaatio hierarkia
        checkOrganisaatioHierarchy(entity, model.getParentOid());

        for (Yhteystieto yhtTieto : entity.getYhteystiedot()) {
            yhtTieto.setOrganisaatio(entity);
        }

        // Generate natural key, OVT-4954
        // "Jos kyseessä on koulutustoimija pitäisi palauttaa y-tunnus."
        // "Jos oppilaitos, palautetaan oppilaitosnumero."
        // "Jos toimipiste, palautetaan oppilaitosnro+toimipisteenjärjestysnumero(konkatenoituna)sekä yhkoulukoodi."
        entity.setToimipisteKoodi(calculateAndUpdateToimipisteKoodi2(entity));

        // call super.insert OR update which saves & validates jpa
        if (updating) {
            LOG.info("updating " + entity);
            organisaatioDAO.update(entity);
            entity = organisaatioDAO.read(entity.getId());
        } else {
            entity = organisaatioDAO.insert(entity);
        }

        /*
         * Saving the parent relationship
         */
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

        //index to solr
        solrIndexer.index(Lists.newArrayList(entity));

        return entity;
    }

    private Organisaatio removeOrganisaatioByOid(String oid, Set<String> reindex) {
        Organisaatio removed = organisaatioDAO.markRemoved(oid);
        reindex.add(oid);
        return removed.getParent();
    }

    @Override
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    public String deleteOrganisaatio(String oid) {
        permissionChecker.checkRemoveOrganisation(oid);
        Set<String> reindex = new HashSet<String>();
        Organisaatio po = removeOrganisaatioByOid(oid, reindex);

        solrIndexer.delete(new ArrayList<String>(reindex));

        // päivittää aliorganisaatioiden lukumäärän parenttiin
        if (po != null) {
            solrIndexer.index(Arrays.asList(po));
        }
        return "{\"message\": \"deleted\"}";
    }

    @Override
    @Secured({"ROLE_APP_ORGANISAATIOHALLINTA"})
    public OrganisaatioRDTO newOrganisaatio(OrganisaatioRDTO ordto) {
        permissionChecker.checkSaveOrganisation(ordto, false);
        try {
            Organisaatio org = save(ordto, false, false);
            OrganisaatioRDTO ret = conversionService.convert(org, OrganisaatioRDTO.class);
            return ret;
        } catch (ValidationException ex) {
            throw new OrganisaatioResourceException(Response.Status.FORBIDDEN, ex.toString());
        }
    }

    public void generateOids(Organisaatio organisaatio) throws ExceptionMessage {
        if (organisaatio.getOid() == null) {
            organisaatio.setOid(oidService.newOid(NodeClassCode.TOIMIPAIKAT));
        }
        for (Yhteystieto curYt : organisaatio.getYhteystiedot()) {
            if (curYt.getYhteystietoOid() == null) {
                curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
            }
        }
    }

    public void generateOidsMetadata(OrganisaatioMetaData omd) throws ExceptionMessage {
        if (omd != null) {
            for (Yhteystieto curYt : omd.getYhteystiedot()) {
                if (curYt != null && curYt.getYhteystietoOid() == null) {
                    curYt.setYhteystietoOid(oidService.newOid(NodeClassCode.TEKN_5));
                }
            }
        }
    }

}
