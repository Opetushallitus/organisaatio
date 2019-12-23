package fi.vm.sade.organisaatio.service.search;

import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import org.springframework.stereotype.Component;

@Component
public class SearchCriteriaService {

    private final SearchCriteriaModelMapper searchCriteriaModelMapper;
    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;
    private final OrganisaatioPermissionServiceImpl organisaatioPermissionService;

    public SearchCriteriaService(SearchCriteriaModelMapper searchCriteriaModelMapper,
                                 OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper,
                                 OrganisaatioPermissionServiceImpl organisaatioPermissionService) {
        this.searchCriteriaModelMapper = searchCriteriaModelMapper;
        this.organisaatioDTOV4ModelMapper = organisaatioDTOV4ModelMapper;
        this.organisaatioPermissionService = organisaatioPermissionService;
    }

    public SearchCriteria getServiceSearchCriteria(OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria) {
        return getServiceSearchCriteria(organisaatioDTOV4ModelMapper.map(apiSearchCriteria,
                OrganisaatioSearchCriteriaDTOV2.class));
    }

    public SearchCriteria getServiceSearchCriteria(OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria) {
        SearchCriteria serviceSearchCriteria = searchCriteriaModelMapper.map(apiSearchCriteria, SearchCriteria.class);
        serviceSearchCriteria.setPoistettu(false);
        serviceSearchCriteria.setPiilotettu(getPiilotettu(apiSearchCriteria.getOid()));
        return serviceSearchCriteria;
    }

    public SearchCriteria getServiceSearchCriteria(OrganisaatioSearchCriteria apiSearchCriteria) {
        SearchCriteria serviceSearchCriteria = searchCriteriaModelMapper.map(apiSearchCriteria, SearchCriteria.class);
        serviceSearchCriteria.setPoistettu(false);
        serviceSearchCriteria.setPiilotettu(getPiilotettu(apiSearchCriteria.getOid()));
        return serviceSearchCriteria;
    }

    public Boolean getPiilotettu() {
        return getPiilotettu(null);
    }

    public Boolean getPiilotettu(String oid) {
        if (!organisaatioPermissionService.isReadAccessToAll()) {
            if (oid == null || !organisaatioPermissionService.userCanReadOrganisation(oid)) {
                return false;
            }
        }
        return null;
    }

}
