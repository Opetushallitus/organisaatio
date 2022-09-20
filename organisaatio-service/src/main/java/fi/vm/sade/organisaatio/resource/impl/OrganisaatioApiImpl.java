package fi.vm.sade.organisaatio.resource.impl;

import com.google.common.base.Preconditions;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioBusinessException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.client.OppijanumeroClient;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.repository.impl.OrganisaatioRepositoryImpl;
import fi.vm.sade.organisaatio.resource.OrganisaatioApi;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.annotation.CheckReadPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ValidationException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Primary
@RestController
@RequestMapping("${server.api.context-path}")
public class OrganisaatioApiImpl implements OrganisaatioApi {
    protected static final Logger LOG = LoggerFactory.getLogger(OrganisaatioApiImpl.class);
    public static final String NOT_AUTHORIZED_TO_READ_ORGANISATION = "Not authorized to read organisation: {}";
    private final OppijanumeroClient oppijanumeroClient;
    private final OrganisaatioResourceV2 organisaatioResourceV2;

    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;
    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;


    protected final PermissionChecker permissionChecker;
    @Autowired
    protected OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;
    private final OrganisaatioBusinessService organisaatioBusinessService;
    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    @Autowired
    public OrganisaatioApiImpl(OrganisaatioResourceV2 organisaatioResourceV2,
                               OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper,
                               PermissionChecker permissionChecker,
                               OrganisaatioBusinessService organisaatioBusinessService,
                               OrganisaatioFindBusinessService organisaatioFindBusinessService,
                               OppijanumeroClient oppijanumeroClient,
                               OrganisaatioNimiModelMapper organisaatioNimiModelMapper) {
        this.organisaatioResourceV2 = organisaatioResourceV2;
        this.organisaatioDTOV4ModelMapper = organisaatioDTOV4ModelMapper;
        this.permissionChecker = permissionChecker;
        this.organisaatioBusinessService = organisaatioBusinessService;
        this.organisaatioFindBusinessService = organisaatioFindBusinessService;
        this.oppijanumeroClient = oppijanumeroClient;
        this.organisaatioNimiModelMapper = organisaatioNimiModelMapper;
    }

    // GET /api/oids?type=KOULUTUSTOIMIJA&count=10&startIndex=100&lastModifiedBefore=X&lastModifiedSince=Y
    @Override
    public List<String> search(OrganisaatioTyyppi type, int count, int startIndex) {
        LOG.debug("search({}, {}, {})", type, count, startIndex);
        List<String> result = organisaatioFindBusinessService.findOidsBy(count, startIndex, type);
        LOG.debug("  result.size = {}", result.size());
        return result;
    }

    /**
     * POST /api/findbyoids
     */
    @Override
    public List<OrganisaatioRDTOV4> findByOids(Set<String> oids) {
        return organisaatioFindBusinessService.findByOidsV4(oids);
    }

    /**
     * GET /api/{oid}/children
     */
    @Override
    public List<OrganisaatioRDTOV4> children(String oid, boolean includeImage) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, String.format("Not authorized to read organisation: %s", oid));
        }
        return this.organisaatioFindBusinessService.findChildrenById(oid, includeImage);
    }

    /**
     * GET /api/{oid}
     */
    @Override
    public OrganisaatioRDTOV4 getOrganisaatioByOID(String oid, boolean includeImage) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, String.format("Not authorized to read organisation: %s", oid));
        }
        return this.organisaatioFindBusinessService.findByIdV4(oid, includeImage);
    }

    /**
     * PUT /api/{oid}
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 updateOrganisaatio(String oid, OrganisaatioRDTOV4 ordto) {
        LOG.info("Saving {}", oid);
        try {
            permissionChecker.checkSaveOrganisation(ordto, true);
        } catch (NotAuthorizedException nae) {
            LOG.warn("NotAuthorizedException for {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            LOG.warn("ValidationException while saving {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("SadeBusinessException while saving {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    sbe.getMessage(), "organisaatio.business.virhe");
        } catch (Exception t) {
            LOG.warn("Throwable while saving {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    /**
     * POST /api/
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 newOrganisaatio(OrganisaatioRDTOV4 ordto) {
        try {
            permissionChecker.checkSaveOrganisation(ordto, false);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to create child organisation for {}", ordto.getParentOid());
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            LOG.warn("ValidationException saving new org");
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("SadeBusinessException saving new org");
            throw new OrganisaatioResourceException(sbe);
        } catch (Exception t) {
            LOG.warn("Throwable saving new org");
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    /**
     * GET /api/muutetut
     */
    @Override
    public List<OrganisaatioRDTOV4> haeMuutetut(
            DateParam lastModifiedSince,
            boolean includeImage,
            List<String> organizationType,
            boolean excludeDiscontinued) {
        try {
            List<OrganisaatioTyyppi> organisaatioTyypit = organizationType == null ? Collections.emptyList() :
                    organizationType.stream().map(OrganisaatioTyyppi::fromKoodiValue).collect(Collectors.toList());
            return this.organisaatioFindBusinessService.haeMuutetut(
                    lastModifiedSince, includeImage, organisaatioTyypit, excludeDiscontinued);
        } catch (IllegalArgumentException iae) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST.value(), iae.getMessage());
        }
    }

    /**
     * GET /api/{oid}/historia
     */
    @Override
    public OrganisaatioHistoriaRDTOV4 getOrganizationHistory(String oid) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        if (oid.equals(rootOrganisaatioOid)) {
            return new OrganisaatioHistoriaRDTOV4();
        }
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.getOrganizationHistory(oid), OrganisaatioHistoriaRDTOV4.class);
    }

    /**
     * GET /api/hae
     */
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatiot(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }

    /**
     * GET /api/hierarkia/hae
     */
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatioHierarkia(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }

    /**
     * GET /api/{oid}/jalkelaiset
     */
    @Override
    public OrganisaatioHakutulosV4 findDescendants(String oid) {
        boolean globalReadAccess = permissionChecker.isReadAccessToAll();
        if (!globalReadAccess) {
            try {
                permissionChecker.checkReadOrganisation(oid);
            } catch (NotAuthorizedException nae) {
                LOG.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
                throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
            }
        }
        return processRows(organisaatioFindBusinessService.findDescendants(oid, globalReadAccess));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioRDTOV4 changeOrganisationRelationship(String oid, String parentOid, boolean merge, DateParam moveDate) {
        Date date = moveDate.getValue();
        try {
            organisaatioBusinessService.mergeOrganisaatio(oid, parentOid, Optional.ofNullable(date), merge);
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error merging organizations {}, {}, {}", oid, parentOid, merge);
            throw new OrganisaatioResourceException(sbe);
        }
        return this.organisaatioFindBusinessService.findByIdV4(oid, false);
    }

    /**
     * DELETE /api/{oid}
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void deleteOrganisaatio(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to delete organisation: {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.UNAUTHORIZED, nae);
        }
        try {
            Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
            LOG.info("Deleted organisaatio: {} under parent: {}", oid, parent.getOid());
        } catch (OrganisaatioNotFoundException e) {
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, e);
        } catch (OrganisaatioBusinessException e) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, e);
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error deleting org {} ", oid);
            throw new OrganisaatioResourceException(sbe);
        }
    }

    /**
     * GET /api/{oid}/paivittaja
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioPaivittajaDTO getOrganisaatioPaivittaja(String oid) {
        if (oid.isBlank()) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, "oid cannot be blank");
        }
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn(NOT_AUTHORIZED_TO_READ_ORGANISATION, oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        Organisaatio org = this.organisaatioFindBusinessService.findById(oid);
        if (org != null) {
            final OrganisaatioPaivittajaDTO tulos = new OrganisaatioPaivittajaDTO();
            tulos.setPaivittaja(org.getPaivittaja());
            tulos.setPaivitysPvm(org.getPaivitysPvm());
            try {
                OppijanumeroClient.OppijanumeroDto henkilo = oppijanumeroClient.henkilo(org.getPaivittaja());
                tulos.setEtuNimet(henkilo.getEtunimet());
                tulos.setSukuNimi(henkilo.getSukunimi());

            } catch (Exception ex) {
                LOG.error(ex.getMessage());
                tulos.setSukuNimi(org.getPaivittaja());
            }
            return tulos;
        } else {
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "not found");
        }

    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioNimiDTO newOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        Preconditions.checkNotNull(oid);
        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }

        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.newOrganisaatioNimi(oid, nimidto);
        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioNimiDTO updateOrganisaatioNimi(String oid, OrganisaatioNimiUpdateDTO nimiUpdateDto) {
        Preconditions.checkNotNull(oid);
        Preconditions.checkNotNull(nimiUpdateDto.getCurrentNimi().getAlkuPvm());
        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }
        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.updateOrganisaatioNimi(oid, nimiUpdateDto);
        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTO.class);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void deleteOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        Preconditions.checkNotNull(oid);
        Preconditions.checkNotNull(nimidto.getAlkuPvm());

        try {
            permissionChecker.checkUpdateOrganisationName(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }

        organisaatioBusinessService.deleteOrganisaatioNimi(oid, nimidto);
    }

    /**
     * PUT /{oid}/tarkasta
     *
     * @param oid of organisationthat was checked
     * @return timestamp when information was checked
     */

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public Timestamp updateTarkastusPvm(String oid) {
        try {
            permissionChecker.checkUpdateOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            throw new OrganisaatioResourceException(nae);
        }
        return organisaatioBusinessService.updateTarkastusPvm(oid);
    }

    // GET /api/{oid}/childoids
    @Override
    @CheckReadPermission
    public List<String> childoids(String oid, boolean rekursiivisesti, boolean aktiiviset, boolean suunnitellut, boolean lakkautetut) {
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
        return childOidList;
    }

    // GET /api/{oid}/parentoids
    @Override
    @CheckReadPermission
    public List<String> parentoids(String oid) {
        // find parents
        return Optional.ofNullable(organisaatioFindBusinessService.findById(oid))
                .map(organisaatio -> Optional.ofNullable(organisaatio.getParentOidPath())
                        .map(parentOidPath -> Stream.concat(Arrays.stream(parentOidPath.split("\\|")), Stream.of(organisaatio.getOid())))
                        .orElseGet(() -> Stream.of(organisaatio.getOid())))
                .orElseGet(() -> Stream.of(rootOrganisaatioOid, oid))
                .filter(StringUtils::hasLength).collect(Collectors.toList());
    }

    // prosessointi tarkoituksella transaktion ulkopuolella
    private static OrganisaatioHakutulosV4 processRows(List<OrganisaatioRepositoryImpl.JalkelaisetRivi> rows) {
        final Set<OrganisaatioPerustietoV4> rootOrgs = new HashSet<>();
        final Map<String, OrganisaatioPerustietoV4> oidToOrg = new HashMap<>();
        OrganisaatioPerustietoV4 current = null;
        Set<String> parentOids = new LinkedHashSet<>(); // linked hash set säilyttää järjestyksen
        for (OrganisaatioRepositoryImpl.JalkelaisetRivi row : rows) {
            if (current == null || !row.oid.equals(current.getOid())) {
                if (current != null) { // edellinen valmis, asetetaan mappiin
                    finalizePerustieto(current, parentOids);
                    oidToOrg.put(current.getOid(), current);
                    parentOids = new LinkedHashSet<>();
                }
                current = new OrganisaatioPerustietoV4();
                current.setMatch(true);
                current.setOid(row.oid);
                current.setAlkuPvm(row.alkuPvm);
                current.setLakkautusPvm(row.lakkautusPvm);
                current.setYtunnus(row.ytunnus);
                current.setVirastoTunnus(row.virastotunnus);
                current.setOppilaitosKoodi(row.oppilaitoskoodi);
                current.setOppilaitostyyppi(row.oppilaitostyyppi);
                current.setToimipistekoodi(row.toimipistekoodi);
                current.setKotipaikkaUri(row.kotipaikka);
                current.setOrganisaatiotyypit(new HashSet<>());
                current.setNimi(new HashMap<>());
                current.setLyhytNimi(new HashMap<>());
                current.setKieletUris(new HashSet<>());
                current.setChildren(new HashSet<>());
                current.setParentOid(row.parentOid);
                OrganisaatioPerustietoV4 parent = oidToOrg.get(row.parentOid);
                if (parent == null) {
                    rootOrgs.add(current);
                } else {
                    parent.getChildren().add(current);
                    parent.setAliOrganisaatioMaara(parent.getChildren().size());
                }
            }
            if (row.parentOid != null) {
                parentOids.add(row.parentOid);
            }
            if (row.organisaatiotyyppi != null) {
                current.getOrganisaatiotyypit().add(row.organisaatiotyyppi);
            }
            if (row.nimiKieli != null) {
                current.getNimi().put(row.nimiKieli, row.nimiArvo);
            }
            if (row.kieli != null) {
                current.getKieletUris().add(row.kieli);
            }
        }
        if (current != null) { // viimeistellään viimeinen käsitelty rivi
            finalizePerustieto(current, parentOids);
            oidToOrg.put(current.getOid(), current);
        }
        OrganisaatioHakutulosV4 result = new OrganisaatioHakutulosV4();
        result.setOrganisaatiot(rootOrgs);
        result.setNumHits(oidToOrg.size());
        return result;
    }

    private static void finalizePerustieto(OrganisaatioPerustietoV4 perustieto, Set<String> parentOids) {
        perustieto.setParentOidPath(generateParentOidPath(parentOids));
    }

    private static String generateParentOidPath(Set<String> parentOids) {
        if (parentOids.isEmpty()) {
            return "";
        }
        List<String> parentOidsList = new ArrayList<>(parentOids);
        Collections.reverse(parentOidsList);
        return String.join("/", parentOidsList);
    }


}
