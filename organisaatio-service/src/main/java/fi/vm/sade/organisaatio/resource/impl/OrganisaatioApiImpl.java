package fi.vm.sade.organisaatio.resource.impl;

import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.SadeBusinessException;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.business.*;
import fi.vm.sade.organisaatio.business.impl.OrganisaatioNimiMasking;
import fi.vm.sade.organisaatio.client.OppijanumerorekisteriClient;
import fi.vm.sade.organisaatio.dto.ChildOidsCriteria;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiDTO;
import fi.vm.sade.organisaatio.dto.OrganisaatioNimiUpdateDTO;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioLiitosModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.v3.GroupModelMapperV3;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioLiitosDTOV2;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import fi.vm.sade.organisaatio.model.OrganisaatioSuhde;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.repository.impl.OrganisaatioRepositoryImpl;
import fi.vm.sade.organisaatio.resource.OrganisaatioApi;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.HakutoimistoDTO;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.service.aspects.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.ValidationException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Primary
@RestController
@RequestMapping("${server.api.context-path}")
@RequiredArgsConstructor
@Slf4j
public class OrganisaatioApiImpl implements OrganisaatioApi {
    private final OppijanumerorekisteriClient oppijanumerorekisteriClient;
    private final OrganisaatioResourceV2 organisaatioResourceV2;

    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;
    private final OrganisaatioNimiModelMapper organisaatioNimiModelMapper;
    private final GroupModelMapperV3 groupModelMapper;
    @Autowired
    private OrganisaatioLiitosModelMapper organisaatioLiitosModelMapper;
    @Autowired
    protected OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;
    private final OrganisaatioBusinessService organisaatioBusinessService;
    private final OrganisaatioNimiService organisaatioNimiService;
    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;
    private final HakutoimistoService hakutoimistoService;

    @Autowired
    private OrganisaatioNimiMasking organisaatioNimiMasking;
    @Autowired
    private ProtectedDataListener protectedDataListener;

    @Value("${root.organisaatio.oid}")
    private String rootOrganisaatioOid;

    // GET /api/oids?type=KOULUTUSTOIMIJA&count=10&startIndex=100&lastModifiedBefore=X&lastModifiedSince=Y
    @Override
    public List<String> oids(OrganisaatioTyyppi type, int count, int startIndex) {
        log.debug("search({}, {}, {})", type, count, startIndex);
        List<String> result = organisaatioFindBusinessService.findOidsBy(count, startIndex, type);
        log.debug("  result.size = {}", result.size());
        return result;
    }

    /**
     * POST /api/findbyoids
     */
    @Override
    public List<OrganisaatioRDTOV4> findByOids(Set<String> oids) {
        List<OrganisaatioRDTOV4> byOidsV4 = organisaatioFindBusinessService.findByOidsV4(oids);
        for (OrganisaatioRDTOV4 organisaatioRDTOV4 : byOidsV4) {
            organisaatioNimiMasking.maskOrganisaatioRDTOV4(organisaatioRDTOV4);
        }
        return byOidsV4;
    }

    /**
     * GET /api/{oid}/children
     */
    @Override
    @CheckReadPermission
    public List<OrganisaatioRDTOV4> children(String oid, boolean includeImage) {
        return this.organisaatioFindBusinessService.findChildrenById(oid, includeImage);
    }

    /**
     * GET /api/{oid}
     */
    @Override
    @CheckReadPermission
    public OrganisaatioRDTOV4 getOrganisaatioByOID(String oid, boolean includeImage) {
        OrganisaatioRDTOV4 org = this.organisaatioFindBusinessService.findByIdV4(oid, includeImage);
        organisaatioNimiMasking.maskOrganisaatioRDTOV4(org);
        return org;
    }

    /**
     * PUT /api/{oid}
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckUpdatePermission
    public ResultRDTOV4 updateOrganisaatio(String oid, OrganisaatioRDTOV4 ordto) {
        log.info("Saving {}", oid);
        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            log.warn("ValidationException while saving {}", oid, ex);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            log.warn("SadeBusinessException while saving {}", oid, sbe);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    sbe.getMessage(), "organisaatio.business.virhe");
        } catch (Exception t) {
            log.warn("Throwable while saving {}", oid, t);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    /**
     * POST /api/
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckAddPermission
    public ResultRDTOV4 newOrganisaatio(OrganisaatioRDTOV4 ordto) {
        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            log.warn("ValidationException saving new org");
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            log.warn("SadeBusinessException saving new org");
            throw new OrganisaatioResourceException(sbe);
        } catch (Exception t) {
            log.warn("Throwable saving new org");
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    /**
     * GET /api/muutetut
     */
    @Override
    public List<OrganisaatioRDTOV4> haeMuutetut(
            LocalDateTime lastModifiedSince,
            boolean includeImage,
            List<String> organizationType,
            boolean excludeDiscontinued) {
        try {
            List<OrganisaatioTyyppi> organisaatioTyypit = organizationType == null ? Collections.emptyList() :
                    organizationType.stream().map(OrganisaatioTyyppi::fromKoodiValue).collect(Collectors.toList());
            List<Organisaatio> organisaatiot = this.organisaatioFindBusinessService.haeMuutetut(
                    lastModifiedSince, organisaatioTyypit, excludeDiscontinued);

            return organisaatioFindBusinessService.mapToOrganisaatioRdtoV4(organisaatiot, false);
        } catch (IllegalArgumentException iae) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST, iae.getMessage());
        }
    }

    // GET /api/muutetut/oid
    @Override
    public List<String> haeMuutettujenOid(
            LocalDateTime lastModifiedSince,
            List<OrganisaatioTyyppi> organisaatioTyypit,
            boolean excludeDiscontinued) {
        return this.organisaatioFindBusinessService.haeMuutetut(lastModifiedSince, organisaatioTyypit, excludeDiscontinued)
            .stream().map(Organisaatio::getOid).collect(Collectors.toList());
    }

    /**
     * GET /api/{oid}/historia
     */
    @Override
    @CheckReadPermission
    public OrganisaatioHistoriaRDTOV4 getOrganizationHistory(String oid) {
        if (oid.equals(rootOrganisaatioOid)) {
            return new OrganisaatioHistoriaRDTOV4();
        }
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.getOrganizationHistory(oid), OrganisaatioHistoriaRDTOV4.class);
    }

    /**
     * GET /api/hierarkia/hae
     */
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        OrganisaatioHakutulosV4 hakutulos = this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatioHierarkia(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
        for (OrganisaatioPerustietoV4 org : hakutulos.getOrganisaatiot()) {
            organisaatioNimiMasking.maskOrganisaatioPerustietoV4(org);
        }
        return hakutulos;
    }

    /**
     * GET /api/{oid}/jalkelaiset
     */
    @Override
    @CheckReadPermission
    public OrganisaatioHakutulosV4 findDescendants(String oid) {
        return processRows(organisaatioFindBusinessService.findDescendants(oid));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioRDTOV4 changeOrganisationRelationship(String oid, String parentOid, boolean merge, LocalDateTime moveDate) {
        Date date = java.sql.Timestamp.valueOf(moveDate);
        try {
            organisaatioBusinessService.mergeOrganisaatio(oid, parentOid, Optional.ofNullable(date), merge);
        } catch (SadeBusinessException sbe) {
            log.warn("Error merging organizations {}, {}, {}", oid, parentOid, merge);
            throw new OrganisaatioResourceException(sbe);
        }
        return this.organisaatioFindBusinessService.findByIdV4(oid, false);
    }

    /**
     * DELETE /api/{oid}
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckDeletePermission
    public void deleteOrganisaatio(String oid) {
        Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
        log.info("Deleted organisaatio: {} under parent: {}", oid, parent.getOid());

    }

    /**
     * GET /api/{oid}/paivittaja
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckReadPermission
    public OrganisaatioPaivittajaDTO getOrganisaatioPaivittaja(String oid) {
        Organisaatio org = this.organisaatioFindBusinessService.findById(oid);
        if (org != null) {
            final OrganisaatioPaivittajaDTO tulos = new OrganisaatioPaivittajaDTO();
            tulos.setPaivittaja(org.getPaivittaja());
            tulos.setPaivitysPvm(org.getPaivitysPvm());
            try {
                var henkilo = oppijanumerorekisteriClient.getHenkilo(org.getPaivittaja());
                tulos.setEtuNimet(henkilo.etunimet());
                tulos.setSukuNimi(henkilo.sukunimi());

            } catch (Exception ex) {
                log.error(ex.getMessage());
                tulos.setSukuNimi(org.getPaivittaja());
            }
            return tulos;
        } else {
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "not found");
        }

    }

    /**
     * POST /{oid}/nimet
     *
     * @param oid
     * @param nimidto
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckUpdateNamePermission
    public OrganisaatioNimiDTO newOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.newOrganisaatioNimi(oid, nimidto);
        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTO.class);
    }

    /**
     * PUT /{oid}/nimet
     *
     * @param oid
     * @param nimiUpdateDto
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckUpdateNamePermission
    public OrganisaatioNimiDTO updateOrganisaatioNimi(String oid, OrganisaatioNimiUpdateDTO nimiUpdateDto) {
        Preconditions.checkNotNull(nimiUpdateDto.getCurrentNimi().getAlkuPvm());
        OrganisaatioNimi organisaatioNimi = organisaatioBusinessService.updateOrganisaatioNimi(oid, nimiUpdateDto);
        return organisaatioNimiModelMapper.map(organisaatioNimi, OrganisaatioNimiDTO.class);
    }

    /**
     * DELETE /{oid}/nimet
     *
     * @param oid
     * @param nimidto
     */
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    @CheckUpdateNamePermission
    public void deleteOrganisaatioNimi(String oid, OrganisaatioNimiDTO nimidto) {
        Preconditions.checkNotNull(nimidto.getAlkuPvm());
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
    @CheckTarkastusPermission
    public Timestamp updateTarkastusPvm(String oid) {
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

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public void authHello() {
        // just check authorization, no implementation needed
    }

    // GET /api/{oid}/nimet
    @Override
    @CheckReadPermission
    public List<OrganisaatioNimiDTO> getOrganisaatioNimet(String oid) {
        List<OrganisaatioNimiDTO> nimet = organisaatioNimiService.getNimet(oid);
        for (OrganisaatioNimiDTO nimi : nimet) {
            organisaatioNimiMasking.maskOrganisaatioNimiDTO(nimi);
        }
        return nimet;
    }

    // GET /api/ryhmat
    @Override
    public List<OrganisaatioGroupDTOV3> groups(RyhmaCriteriaDtoV3 criteria) {
        return groupModelMapper.map(organisaatioFindBusinessService.findGroups(criteria), new TypeToken<List<OrganisaatioGroupDTOV3>>() {
        }.getType());
    }

    // GET /api/liitokset
    @Override
    public List<OrganisaatioLiitosDTOV2> liitokset() {
        List<OrganisaatioSuhde> liitokset = organisaatioFindBusinessService.findLiitokset(null);
        Type organisaatioLiitosType = new TypeToken<List<OrganisaatioLiitosDTOV2>>() {}.getType();
        List<OrganisaatioLiitosDTOV2> result = organisaatioLiitosModelMapper.map(liitokset, organisaatioLiitosType);
        for (OrganisaatioLiitosDTOV2 liitos : result) {
            organisaatioNimiMasking.maskOrganisaatioLiitosDTOV2(liitos);
        }
        return result;
    }

    // GET /api/{oid}/hakutoimisto
    @Override
    @CheckReadPermission
    public HakutoimistoDTO hakutoimisto(String organisaatioOid) {
        return hakutoimistoService.hakutoimisto(organisaatioOid);
    }

    // prosessointi tarkoituksella transaktion ulkopuolella
    private OrganisaatioHakutulosV4 processRows(List<OrganisaatioRepositoryImpl.JalkelaisetRivi> rows) {
        final Set<OrganisaatioPerustietoV4> rootOrgs = new TreeSet<>(Comparator.comparing(OrganisaatioPerustietoV4::getOid));
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
                boolean isProtectedOrg = row.piilotettu || ProtectedDataListener.YKSITYINEN_ELINKEINOHARJOITTAJA.equals(row.yritysmuoto);
                current.setMaskingActive(isProtectedOrg && !protectedDataListener.canViewProtected());
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
                current.setKieletUris(new LinkedHashSet<>());
                current.setChildren(new LinkedHashSet<>());
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
        return OrganisaatioHakutulosV4.builder()
                .organisaatiot(rootOrgs)
                .numHits(oidToOrg.size())
                .build();
    }

    private void finalizePerustieto(OrganisaatioPerustietoV4 perustieto, Set<String> parentOids) {
        perustieto.setParentOidPath(generateParentOidPath(parentOids));
        organisaatioNimiMasking.maskOrganisaatioPerustietoV4(perustieto);
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
