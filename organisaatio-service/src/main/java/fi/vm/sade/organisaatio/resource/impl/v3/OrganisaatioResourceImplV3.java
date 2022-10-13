package fi.vm.sade.organisaatio.resource.impl.v3;

import com.google.common.base.Preconditions;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.dto.mapping.v3.GroupModelMapperV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioGroupDTOV3;
import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.dto.RyhmaCriteriaDtoV3;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("${server.rest.context-path}/organisaatio/v3")
public class OrganisaatioResourceImplV3 implements OrganisaatioResourceV3 {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioResourceImplV3.class);


    @Autowired
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Autowired
    private GroupModelMapperV3 groupModelMapper;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private OrganisaatioRepository organisaatioRepository;

    @Autowired
    private PermissionChecker permissionChecker;

    @Override
    public List<OrganisaatioRDTOV3> findByOids(List<String> oids) {
        Preconditions.checkNotNull(oids);
        Preconditions.checkArgument(!oids.isEmpty());
        Preconditions.checkArgument(oids.size() <= 1000);
        return organisaatioFindBusinessService.findByOids(oids);
    }

    // GET /organisaatio/v3/{oid}/children
    @Override
    public List<OrganisaatioRDTOV3> children(String oid, boolean includeImage) throws Exception {
        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

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
    public List<OrganisaatioGroupDTOV3> groups(RyhmaCriteriaDtoV3 criteria) throws Exception {
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> entitys = organisaatioFindBusinessService.findGroups(criteria);

        LOG.debug("Ryhmien haku {} ms", System.currentTimeMillis() - qstarted);
        long qstarted2 = System.currentTimeMillis();

        Type groupListType = new TypeToken<List<OrganisaatioGroupDTOV3>>() {
        }.getType();

        List<OrganisaatioGroupDTOV3> groupList = groupModelMapper.map(entitys, groupListType);

        LOG.debug("Ryhmien convertointi {} ms --> yhteensä {} ms", System.currentTimeMillis() - qstarted2, System.currentTimeMillis() - qstarted);

        return groupList;
    }

    // GET /organisaatio/v3/<oid>
    @Override
    public OrganisaatioRDTOV3 getOrganisaatioByOID(String oid, boolean includeImage) {
        LOG.debug("/organisaatio/{} -- getOrganisaatioByOID()", oid);

        try {
            permissionChecker.checkReadOrganisation(oid);
        } catch (NotAuthorizedException nae) {
            LOG.warn("Not authorized to read organisation: {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        Organisaatio o = organisaatioFindBusinessService.findById(oid);

        if (o == null) {
            LOG.info("Failed to find organisaatio by: {}", oid);
            throw new OrganisaatioResourceException(HttpStatus.NOT_FOUND, "organisaatio.exception.organisaatio.not.found");
        }

        // Jätetään kuva pois, jos sitä ei haluta
        if (o.getMetadata() != null) {
            o.getMetadata().setIncludeImage(includeImage);
        }

        OrganisaatioRDTOV3 result = conversionService.convert(o, OrganisaatioRDTOV3.class);

        LOG.debug("  result={}", result);
        return result;
    }

    // GET /organisaatio/v3/muutetut
    @Override
    public List<OrganisaatioRDTOV3> haeMuutetut(LocalDateTime lastModifiedSince, boolean includeImage) {
        Preconditions.checkNotNull(lastModifiedSince);

        LOG.debug("haeMuutetut: {}", lastModifiedSince);
        long qstarted = System.currentTimeMillis();

        List<Organisaatio> organisaatiot = organisaatioRepository.findModifiedSince(
                !permissionChecker.isReadAccessToAll(), lastModifiedSince);

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
