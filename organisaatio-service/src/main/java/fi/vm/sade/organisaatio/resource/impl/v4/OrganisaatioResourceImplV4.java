package fi.vm.sade.organisaatio.resource.impl.v4;

import fi.vm.sade.organisaatio.SadeBusinessException;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.HakutoimistoService;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioNimiService;
import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.client.OppijanumeroClient;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioNimiModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.v3.GroupModelMapperV3;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.resource.OrganisaatioResourceException;
import fi.vm.sade.organisaatio.resource.impl.OrganisaatioApiImpl;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("${server.rest.context-path}/organisaatio/v4")
public class OrganisaatioResourceImplV4 extends OrganisaatioApiImpl implements
        OrganisaatioResourceV4 {
    protected final PermissionChecker permissionChecker;

    public OrganisaatioResourceImplV4(OrganisaatioResourceV2 organisaatioResourceV2, OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper, PermissionChecker permissionChecker, OrganisaatioBusinessService organisaatioBusinessService, OrganisaatioNimiService organisaatioNimiService, OrganisaatioFindBusinessService organisaatioFindBusinessService, HakutoimistoService hakutoimistoService, OppijanumeroClient oppijanumeroClient, OrganisaatioNimiModelMapper organisaatioNimiModelMapper, GroupModelMapperV3 groupModelMapper) {
        super(oppijanumeroClient, organisaatioResourceV2, organisaatioDTOV4ModelMapper, organisaatioNimiModelMapper, groupModelMapper, organisaatioBusinessService, organisaatioNimiService, organisaatioFindBusinessService, hakutoimistoService);
        this.permissionChecker = permissionChecker;
    }

    @Override
    @PreAuthorize("hasRole('ROLE_APP_ORGANISAATIOHALLINTA') or hasRole('ROLE_APP_ORGANISAATIOHALLINTA_CRUD')")
    public String deleteOrganisaatioOld(String oid) {
        try {
            permissionChecker.checkRemoveOrganisation();
        } catch (NotAuthorizedException nae) {
            log.warn("Not authorized to delete organisation: " + oid);
            throw new OrganisaatioResourceException(HttpStatus.FORBIDDEN, nae);
        }

        try {
            Organisaatio parent = organisaatioDeleteBusinessService.deleteOrganisaatio(oid);
            log.info("Deleted organisaatio: " + oid + " under parent: " + parent.getOid());
        } catch (SadeBusinessException sbe) {
            log.warn("Error deleting org", sbe);
            throw new OrganisaatioResourceException(sbe);
        }

        return "{\"message\": \"deleted\"}";
    }
}
