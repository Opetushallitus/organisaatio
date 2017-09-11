package fi.vm.sade.organisaatio.resource.impl.v3;

import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.api.DateParam;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dao.OrganisaatioDAO;
import fi.vm.sade.organisaatio.dto.mapping.v3.GroupModelMapperV3;
import fi.vm.sade.organisaatio.dto.mapping.v3.OrganisaatioRDTOMapperV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.dto.v3.ResultRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    private OrganisaatioResource organisaatioResource;

    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    private GroupModelMapperV3 groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioDAO organisaatioDAO;

    @Autowired
    private OrganisaatioRDTOMapperV3 organisaatioRDTOMapperV3;

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
        OrganisaatioRDTO dtov1 = organisaatioRDTOMapperV3.map(ordto, OrganisaatioRDTO.class);
        ResultRDTO result = organisaatioResource.updateOrganisaatio(oid, dtov1);
        OrganisaatioRDTOV3 dtov3 = organisaatioRDTOMapperV3.map(result.getOrganisaatio(), OrganisaatioRDTOV3.class);
        return new ResultRDTOV3(dtov3, result.getInfo());
    }

    // POST /organisaatio/v3/
    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA')")
    public ResultRDTOV3 newOrganisaatio(OrganisaatioRDTOV3 ordto) {
        OrganisaatioRDTO dtov1 = organisaatioRDTOMapperV3.map(ordto, OrganisaatioRDTO.class);
        ResultRDTO result = organisaatioResource.newOrganisaatio(dtov1);
        OrganisaatioRDTOV3 dtov3 = organisaatioRDTOMapperV3.map(result.getOrganisaatio(), OrganisaatioRDTOV3.class);
        return new ResultRDTOV3(dtov3, result.getInfo());
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
