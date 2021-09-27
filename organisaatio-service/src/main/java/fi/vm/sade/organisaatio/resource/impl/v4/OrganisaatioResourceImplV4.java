package fi.vm.sade.organisaatio.resource.impl.v4;

import com.google.common.base.Preconditions;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.business.exception.OrganisaatioNotFoundException;
import fi.vm.sade.organisaatio.repository.impl.OrganisaatioRepositoryImpl;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organisaatio/v4")
public class OrganisaatioResourceImplV4 implements OrganisaatioResourceV4 {
    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImplV4.class);

    private final OrganisaatioResourceV2 organisaatioResourceV2;
    private final OrganisaatioResourceV3 organisaatioResourceV3;

    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;

    private final PermissionChecker permissionChecker;
    private final OrganisaatioBusinessService organisaatioBusinessService;
    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    public OrganisaatioResourceImplV4(OrganisaatioResourceV2 organisaatioResourceV2,
                                      OrganisaatioResourceV3 organisaatioResourceV3,
                                      OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper,
                                      PermissionChecker permissionChecker,
                                      OrganisaatioBusinessService organisaatioBusinessService,
                                      OrganisaatioFindBusinessService organisaatioFindBusinessService) {
        this.organisaatioResourceV2 = organisaatioResourceV2;
        this.organisaatioResourceV3 = organisaatioResourceV3;
        this.organisaatioDTOV4ModelMapper = organisaatioDTOV4ModelMapper;
        this.permissionChecker = permissionChecker;
        this.organisaatioBusinessService = organisaatioBusinessService;
        this.organisaatioFindBusinessService = organisaatioFindBusinessService;
    }

    // POST //organisaatio/v4/findbyoids
    @Override
    public List<OrganisaatioRDTOV4> findByOids(Set<String> oids){
        return organisaatioFindBusinessService.findByOidsV4(oids);
    }

    // GET /organisaatio/v4/{oid}/children
    @Override
    public List<OrganisaatioRDTOV4> children(String oid, boolean includeImage) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        return this.organisaatioFindBusinessService.findChildrenById(oid, includeImage);
    }

    // GET /organisaatio/v4/{oid}
    @Override
    public OrganisaatioRDTOV4 getOrganisaatioByOID(String oid, boolean includeImage) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        return this.organisaatioFindBusinessService.findByIdV4(oid, includeImage);
    }

    // PUT /organisaatio/v4/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 updateOrganisaatio(String oid, OrganisaatioRDTOV4 ordto) {
        LOG.info("Saving " + oid);
        try {
            permissionChecker.checkSaveOrganisation(ordto, true);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to update organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            LOG.warn("Error saving " + oid, ex);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving " + oid, sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (OrganisaatioResourceException ore) {
            LOG.warn("Error saving " + oid, ore);
            throw ore;
        } catch (Throwable t) {
            LOG.error("Error saving " + oid, t);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    // DELETE /organisaatio/v4/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatio(String oid) {
        return this.organisaatioResourceV3.deleteOrganisaatio(oid);
    }

    // POST /organisaatio/v4/
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV4 newOrganisaatio(OrganisaatioRDTOV4 ordto) {
        try {
            permissionChecker.checkSaveOrganisation(ordto, false);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to create child organisation for: " + ordto.getParentOid());
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        try {
            return organisaatioBusinessService.saveOrUpdate(ordto);
        } catch (ValidationException ex) {
            LOG.warn("Error saving new org", ex);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving new org", sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (Throwable t) {
            LOG.warn("Error saving new org", t);
            throw new OrganisaatioResourceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    // GET /organisaatio/v4/muutetut
    @Override
    public List<OrganisaatioRDTOV4> haeMuutetut(
            DateParam lastModifiedSince,
            boolean includeImage,
            List<String> organizationTypes,
            boolean excludeDiscontinued) {
        try {
            List<OrganisaatioTyyppi> organisaatioTyypit = organizationTypes == null ? Collections.emptyList() :
                    organizationTypes.stream().map(OrganisaatioTyyppi::fromKoodiValue).collect(Collectors.toList());
            return this.organisaatioFindBusinessService.haeMuutetut(
                    lastModifiedSince, includeImage, organisaatioTyypit, excludeDiscontinued);
        } catch (IllegalArgumentException iae) {
            throw new OrganisaatioResourceException(HttpStatus.BAD_REQUEST.value(), iae.getMessage());
        }
    }

    // GET /organisaatio/v4/{oid}/historia
    @Override
    public OrganisaatioHistoriaRDTOV4 getOrganizationHistory(String oid) throws Exception {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.getOrganizationHistory(oid), OrganisaatioHistoriaRDTOV4.class);
    }

    // GET /organisaatio/v4/hae
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatiot(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }

    // GET /organisaatio/v4/hierarkia/hae
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatioHierarkia(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        OrganisaatioSearchCriteriaDTOV2 organisaatioSearchCriteriaDTOV2 = this.organisaatioDTOV4ModelMapper.map(hakuEhdot, OrganisaatioSearchCriteriaDTOV2.class);
        return this.organisaatioDTOV4ModelMapper.map(this.organisaatioResourceV2.searchOrganisaatioHierarkia(organisaatioSearchCriteriaDTOV2), OrganisaatioHakutulosV4.class);
    }

    // GET /organisaatio/v4/{oid}/jalkelaiset
    @Override
    public OrganisaatioHakutulosV4 findDescendants(String oid) {
        boolean globalReadAccess = permissionChecker.isReadAccessToAll();
        if (!globalReadAccess) {
            try {
                permissionChecker.checkReadOrganisation(oid);
            } catch (NotAuthorizedException nae) {
                LOG.warn("Not authorized to read organisation: " + oid);
                throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
            }
        }
        return processRows(organisaatioFindBusinessService.findDescendants(oid, globalReadAccess));
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public OrganisaatioRDTOV4 changeOrganisationRelationship(String oid, String parentOid, boolean merge, DateParam dateParam) {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        try {
            permissionChecker.checkReadOrganisation(parentOid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: " + parentOid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }
        Date date;
        if (dateParam != null && dateParam.getValue() != null) {
            date = dateParam.getValue();
        } else {
            date = new Date();
        }

        OrganisaatioRDTOV4 organisaatio = this.organisaatioFindBusinessService.findByIdV4(oid, false);
        OrganisaatioRDTOV4 newParent = this.organisaatioFindBusinessService.findByIdV4(parentOid,false);

        if (organisaatio == null) {
            throw new OrganisaatioNotFoundException(oid);
        }
        if (newParent == null) {
            throw new OrganisaatioNotFoundException(parentOid);
        }

        // Liitetäänkö organisaatio vai siirretäänkö organisaatio
        try {
            if (merge) {
                // Organisaatio yhdistyy toiseen, yhdistyvä organisaatio passivoidaan
                organisaatioBusinessService.mergeOrganisaatio(oid, parentOid, date);
            } else {
                // Oppilaitos siirtyy toisen organisaation alle
                organisaatioBusinessService.changeOrganisaatioParent(oid, parentOid, date);
            }
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving multiple organizations", sbe);
            throw new OrganisaatioResourceException(sbe);
        }
        return organisaatio;
    }

    // prosessointi tarkoituksella transaktion ulkopuolella
    private static OrganisaatioHakutulosV4 processRows(List<OrganisaatioRepositoryImpl.JalkelaisetRivi> rows) {
        final Set<OrganisaatioPerustietoV4> rootOrgs = new HashSet<>();
        final Map<String,OrganisaatioPerustietoV4> oidToOrg = new HashMap<>();
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
