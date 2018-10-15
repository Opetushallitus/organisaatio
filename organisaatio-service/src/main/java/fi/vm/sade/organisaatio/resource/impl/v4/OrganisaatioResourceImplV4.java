package fi.vm.sade.organisaatio.resource.impl.v4;

import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v4.*;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
@CrossOriginResourceSharing(allowAllOrigins = true)
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
    public List<OrganisaatioRDTOV4> findByOids(List<String> oids){
        return organisaatioFindBusinessService.findByOidsV4(oids);
    }

    // GET /organisaatio/v4/{oid}/children
    @Override
    public List<OrganisaatioRDTOV4> children(String oid, boolean includeImage) {
        return this.organisaatioFindBusinessService.findChildrenById(oid, includeImage);
    }

    // GET /organisaatio/v4/{oid}
    @Override
    public OrganisaatioRDTOV4 getOrganisaatioByOID(String oid, boolean includeImage) {
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
            throw new OrganisaatioResourceException(nae);
        }

        try {
            return organisaatioBusinessService.save(ordto, true);
        } catch (ValidationException ex) {
            LOG.warn("Error saving " + oid, ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving " + oid, sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (OrganisaatioResourceException ore) {
            LOG.warn("Error saving " + oid, ore);
            throw ore;
        } catch (Throwable t) {
            LOG.error("Error saving " + oid, t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
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
            throw new OrganisaatioResourceException(nae);
        }
        try {
            return organisaatioBusinessService.save(ordto, false);
        } catch (ValidationException ex) {
            LOG.warn("Error saving new org", ex);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getMessage(), "organisaatio.validointi.virhe");
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error saving new org", sbe);
            throw new OrganisaatioResourceException(sbe);
        } catch (Throwable t) {
            LOG.warn("Error saving new org", t);
            throw new OrganisaatioResourceException(Response.Status.INTERNAL_SERVER_ERROR,
                    t.getMessage(), "generic.error");
        }
    }

    // GET /organisaatio/v4/muutetut
    @Override
    public List<OrganisaatioRDTOV4> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {
        return this.organisaatioFindBusinessService.haeMuutetut(lastModifiedSince, includeImage);
    }

    // GET /organisaatio/v4/{oid}/historia
    @Override
    public OrganisaatioHistoriaRDTOV4 getOrganizationHistory(String oid) throws Exception {
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
}
