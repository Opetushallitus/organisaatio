package fi.vm.sade.organisaatio.resource;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioHakutulos;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.api.util.OrganisaatioPerustietoUtil;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.helper.OrganisaatioDisplayHelper;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.YhteystietojenTyyppi;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.YhteystietojenTyyppiRepository;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.resource.dto.YhteystietojenTyyppiRDTO;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.search.SearchCriteriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@RestController
@RequestMapping("${server.rest.context-path}/organisaatio")
public class OrganisaatioResourceImpl implements OrganisaatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImpl.class);
    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;
    @Autowired
    private OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;
    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;
    @Autowired
    private YhteystietojenTyyppiRepository yhteystietojenTyyppiRepository;
    @Autowired
    private OrganisaatioRepository organisaatioRepository;
    @Autowired
    private ConversionService conversionService;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Autowired
    PermissionChecker permissionChecker;

    @Autowired
    private SearchCriteriaService searchCriteriaService;

    // GET /organisaatio/hae
    @Override
    public OrganisaatioHakutulos searchHierarchy(OrganisaatioSearchCriteria s) {
        final OrganisaatioHakutulos tulos = new OrganisaatioHakutulos();

        if (s.getOppilaitostyyppi() != null && s.getOppilaitostyyppi().isEmpty()) {
            s.setOppilaitostyyppi(null);
        }

        if (s.getOrganisaatiotyyppi() != null && s.getOrganisaatiotyyppi().length() == 0) {
            s.setOrganisaatiotyyppi(null);
        }

        // Map api search criteria to service search criteria
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(s);
        SearchConfig searchConfig = new SearchConfig(!s.getSkipParents(), true, true);

        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);

        //sorttaa
        final Ordering<OrganisaatioPerustieto> ordering = Ordering.natural()
                .nullsFirst()
                .onResultOf((Function<OrganisaatioPerustieto, Comparable<String>>) input -> OrganisaatioDisplayHelper.getClosestBasic(I18N.getLocale(), input));

        organisaatiot = ordering.immutableSortedCopy(organisaatiot);

        //rakenna hierarkia
        tulos.setOrganisaatiot(OrganisaatioPerustietoUtil.createHierarchy(organisaatiot));

        tulos.setNumHits(organisaatiot.size());
        return tulos;
    }

    // GET /organisaatio/{oid}/children
    @Override
    @Transactional
    public List<OrganisaatioRDTO> children(String oid, boolean includeImage) throws Exception {
        Preconditions.checkNotNull(oid);
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        Organisaatio parentOrg = organisaatioFindBusinessService.findById(oid);
        List<OrganisaatioRDTO> childList = new LinkedList<>();
        if (parentOrg != null) {
            for (Organisaatio child : parentOrg.getChildren(true)) {
                // Jätetään kuva pois, jos sitä ei haluta
                if (child.getMetadata() != null) {
                    child.getMetadata().setIncludeImage(includeImage);
                }

                childList.add(conversionService.convert(child, OrganisaatioRDTO.class));
            }
        }
        return childList;
    }

    // GET /organisaatio/{oid}/childoids
    @Override
    public String childoids(String oid, boolean rekursiivisesti, boolean aktiiviset, boolean suunnitellut, boolean lakkautetut) throws Exception {
        Preconditions.checkNotNull(oid);
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        List<String> childOidList = new LinkedList<>();
        if (rekursiivisesti) {
            ChildOidsCriteria criteria = new ChildOidsCriteria(oid, aktiiviset, suunnitellut, lakkautetut, LocalDate.now());
            childOidList.addAll(organisaatioFindBusinessService.findChildOidsRecursive(criteria));
        } else {
            Organisaatio parentOrg = organisaatioFindBusinessService.findById(oid);
            if (parentOrg != null) {
                for (Organisaatio child : parentOrg.getChildren(aktiiviset, suunnitellut, lakkautetut)) {
                    childOidList.add(child.getOid());
                }
            }
        }
        return "{ \"oids\": [" + childOidList.stream().map(childOid -> "\"" + childOid + "\"").collect(joining(",")) + "]}";
    }

    // GET /organisaatio/{oid}/parentoids - used for security purposes
    @Override
    public String parentoids(String oid) throws Exception {
        Preconditions.checkNotNull(oid);
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        // find parents
        return Optional.ofNullable(organisaatioFindBusinessService.findById(oid))
                .map(organisaatio -> Optional.ofNullable(organisaatio.getParentOidPath())
                        .map(parentOidPath -> Stream.concat(Arrays.stream(parentOidPath.split("\\|")), Stream.of(organisaatio.getOid())))
                        .orElseGet(() -> Stream.of(organisaatio.getOid())))
                .orElseGet(() -> Stream.of(rootOrganisaatioOid, oid))
                .filter(StringUtils::hasLength)
                .collect(joining(OID_SEPARATOR));
    }

    // GET /organisaatio/hello
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

        // lastModifiedBefore ja lastModifiedSince jätetään pois --> muutetut organisaatiot rajapinta palauttaa nuo
        List<String> result = organisaatioFindBusinessService.findOidsBy(count, startIndex, type);
        LOG.debug("  result.size = {}", result.size());
        return result;
    }

    // GET /organisaatio/{oid}
    @Override
    @Transactional
    public OrganisaatioRDTO getOrganisaatioByOID(final String oid, boolean includeImage) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        Organisaatio o = organisaatioFindBusinessService.findById(oid);

        if (o == null) {
            LOG.info("Failed to find organisaatio by: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "organisaatio.exception.organisaatio.not.found");
        }

        // Jätetään kuva pois, jos sitä ei haluta
        if (o.getMetadata() != null) {
            o.getMetadata().setIncludeImage(includeImage);
        }

        OrganisaatioRDTO result = mapToOrganisaatioRdto(o);

        LOG.debug("  result={}", result);
        return result;
    }
    private OrganisaatioRDTO mapToOrganisaatioRdto(Organisaatio organisaatio) {
       return conversionService.convert(organisaatio, OrganisaatioRDTO.class);

    }
    // GET /organisaatio/yhteystietometadata
    @Override
    @Transactional(readOnly = true)
    public Set<YhteystietojenTyyppiRDTO> getYhteystietoMetadata(Set<String> organisaatioTyyppi) {
        if (organisaatioTyyppi == null || organisaatioTyyppi.isEmpty()) {
            return new HashSet<>();
        }
        List<YhteystietojenTyyppi> entitys = yhteystietojenTyyppiRepository.findLisatietoMetadataForOrganisaatio(OrganisaatioTyyppi.fromValueToKoodi(organisaatioTyyppi));
        if (entitys == null) {
            return null;
        }
        Set<YhteystietojenTyyppiRDTO> result = new HashSet<>();
        for (YhteystietojenTyyppi entity : entitys) {
            result.add(conversionService.convert(entity, YhteystietojenTyyppiRDTO.class));
        }
        return result;
    }

    // GET /organisaatio/auth
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String authHello() {
        return "{\"message\": \"Well Hello! " + new Date() + "\"}";
    }

    // GET /organisaatio/{oid}/ryhmat
    @Override
    public List<OrganisaatioRDTO> groups(String oid, boolean includeImage) throws Exception {
        Preconditions.checkNotNull(oid);
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups(new RyhmaCriteriaDtoV3());
        if (entitys == null) {
            return null;
        }

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        List<OrganisaatioRDTO> groupList = new ArrayList<>();
        for (Organisaatio entity : entitys) {
            // Jätetään kuva pois, jos sitä ei haluta
            if (entity.getMetadata() != null) {
                entity.getMetadata().setIncludeImage(includeImage);
            }

            groupList.add(conversionService.convert(entity, OrganisaatioRDTO.class));
        }

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }
}
