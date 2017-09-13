package fi.vm.sade.organisaatio.resource.impl.v3;

import com.google.common.base.Preconditions;
import fi.vm.sade.generic.service.exception.SadeBusinessException;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioDeleteBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.v3.GroupModelMapperV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v3.ResultRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatioResult;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@CrossOriginResourceSharing(allowAllOrigins = true)
public class OrganisaatioResourceImplV3 implements OrganisaatioResourceV3 {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImplV3.class);

    @Autowired
    private OrganisaatioBusinessService organisaatioBusinessService;

    @Autowired
    private OrganisaatioDeleteBusinessService organisaatioDeleteBusinessService;

    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    private GroupModelMapperV3 groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private PermissionChecker permissionChecker;

    // GET /organisaatio/v3/{oid}/children
    @Override
    public List<OrganisaatioRDTOV3> children(String oid, boolean includeImage) throws Exception {
        Preconditions.checkNotNull(oid);
        Organisaatio parentOrg = organisaatioFindBusinessService.findById(oid);
        List<OrganisaatioRDTOV3> childList = new LinkedList<>();
        if (parentOrg != null) {
            for (Organisaatio child : parentOrg.getChildren(true)) {
                // Jätetään kuva pois, jos sitä ei haluta
                if (child.getMetadata() != null) {
                    child.getMetadata().setIncludeImage(includeImage);
                }

                childList.add(conversionService.convert(child, OrganisaatioRDTOV3.class));
            }
        }
        return childList;
    }

    // GET /organisaatio/v3/ryhmat
    @Override
    public List<OrganisaatioGroupDTOV3> groups() throws Exception {
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups();

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        Type groupListType = new TypeToken<List<OrganisaatioGroupDTOV3>>() {}.getType();

        List<OrganisaatioGroupDTOV3> groupList = groupModelMapper.map(entitys, groupListType);

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }

    // GET /organisaatio/v3/{oid}
    @Override
    public OrganisaatioRDTOV3 getOrganisaatioByOID(String oid, boolean includeImage) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);

        Organisaatio o = organisaatioFindBusinessService.findById(oid);

        if (o == null) {
            LOG.info("Failed to find organisaatio by: " + oid);
            throw new OrganisaatioResourceException(404, "organisaatio.exception.organisaatio.not.found");
        }

        // Jätetään kuva pois, jos sitä ei haluta
        if (o.getMetadata() != null) {
            o.getMetadata().setIncludeImage(includeImage);
        }

        OrganisaatioRDTOV3 result = conversionService.convert(o, OrganisaatioRDTOV3.class);

        LOG.debug("  result={}", result);
        return result;
    }

    // PUT /organisaatio/v3/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV3 updateOrganisaatio(String oid, OrganisaatioRDTOV3 ordto) {
        LOG.info("Saving " + oid);
        try {
            permissionChecker.checkSaveOrganisation(ordto, true);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to update organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, true);
            return new ResultRDTOV3(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTOV3.class), result.getInfo());
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

    // DELETE /organisaatio/v3/{oid}
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public String deleteOrganisaatio(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to delete organisation: " + oid);
            throw new OrganisaatioResourceException(nae);
        }

        try {
            Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
            LOG.info("Deleted organisaatio: " + oid +" under parent: " + parent.getOid());
        } catch (SadeBusinessException sbe) {
            LOG.warn("Error deleting org", sbe);
            throw new OrganisaatioResourceException(sbe);
        }

        return "{\"message\": \"deleted\"}";
    }

    // POST /organisaatio/v3/
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV3 newOrganisaatio(OrganisaatioRDTOV3 ordto) {
        try {
            permissionChecker.checkSaveOrganisation(ordto, false);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to create child organisation for: " + ordto.getParentOid());
            throw new OrganisaatioResourceException(nae);
        }
        try {
            OrganisaatioResult result = organisaatioBusinessService.save(ordto, false);
            return new ResultRDTOV3(conversionService.convert(result.getOrganisaatio(), OrganisaatioRDTOV3.class), result.getInfo());
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

    // GET /organisaatio/v3/muutetut
    @Override
    public List<OrganisaatioRDTOV3> haeMuutetut(DateParam lastModifiedSince, boolean includeImage) {
        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: " + lastModifiedSince.toString());
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioDAO.findModifiedSince(lastModifiedSince.getValue());

        LOG.debug("Muutettujen haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        if (organisaatiot == null || organisaatiot.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrganisaatioRDTOV3> results = new ArrayList<>();

        for (Organisaatio org : organisaatiot) {
            // Jätetään kuva pois, jos sitä ei haluta
            if (org.getMetadata() != null) {
                org.getMetadata().setIncludeImage(includeImage);
            }

            OrganisaatioRDTOV3 result = conversionService.convert(org, OrganisaatioRDTOV3.class);
            results.add(result);
        }

        LOG.debug("Muutettujen convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return results;
    }

}
