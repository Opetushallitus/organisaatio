package fi.vm.sade.organisaatio.resource.impl.v4;

import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.resource.impl.OrganisaatioApiImpl;
import fi.vm.sade.organisaatio.resource.v2.OrganisaatioResourceV2;
import fi.vm.sade.organisaatio.resource.v3.OrganisaatioResourceV3;
import fi.vm.sade.organisaatio.resource.v4.OrganisaatioResourceV4;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${server.rest.context-path}/organisaatio/v4")
public class OrganisaatioResourceImplV4 extends OrganisaatioApiImpl implements
        OrganisaatioResourceV4 {

    public OrganisaatioResourceImplV4(OrganisaatioResourceV2 organisaatioResourceV2, OrganisaatioResourceV3 organisaatioResourceV3, OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper, PermissionChecker permissionChecker, OrganisaatioBusinessService organisaatioBusinessService, OrganisaatioFindBusinessService organisaatioFindBusinessService) {
        super(organisaatioResourceV2, organisaatioResourceV3, organisaatioDTOV4ModelMapper, permissionChecker, organisaatioBusinessService, organisaatioFindBusinessService);
    }
}
