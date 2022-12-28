package fi.vm.sade.organisaatio.resource.impl;

import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioHakutulosV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioPerustietoV4;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import fi.vm.sade.organisaatio.resource.OrganisaatioSearchApi;
import fi.vm.sade.organisaatio.resource.dto.HakuTulos;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import fi.vm.sade.organisaatio.service.search.SearchCriteriaService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Hidden
@RequestMapping({"${server.api.context-path}/hae", "${server.rest.context-path}/organisaatio/v4/hae"})
@RequiredArgsConstructor
@Slf4j
public class OrganisaatioSearchApiImpl implements OrganisaatioSearchApi {

    private final OrganisaatioFindBusinessService organisaatioFindBusinessService;
    private final OrganisaatioDTOV4ModelMapper organisaatioDTOV4ModelMapper;
    private final SearchCriteriaService searchCriteriaService;

    /**
     * GET /api/hae
     */
    @Override
    public OrganisaatioHakutulosV4 searchOrganisaatiot(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
        SearchConfig searchConfig = new SearchConfig(false, false, true);
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);
        return OrganisaatioHakutulosV4.<OrganisaatioPerustietoV4>builder().numHits(organisaatiot.size()).organisaatiot(organisaatiot.stream()
                .map(a -> organisaatioDTOV4ModelMapper.map(a, OrganisaatioPerustietoV4.class))
                .sorted(Comparator.comparing(OrganisaatioPerustietoV4::getOid))
                .collect(Collectors.toCollection(LinkedHashSet::new))).build();
    }

    // GET /api/hae/nimi
    @Override
    public HakuTulos<OrganisaatioPerustietoV4> searchOrganisaatiotNimet(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        return search(hakuEhdot);
    }

    // GET /api/hae/nimi
    @Override
    public HakuTulos<OrganisaatioPerustietoV4> searchOrganisaatiotTyypit(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        return search(hakuEhdot);
    }

    private HakuTulos<OrganisaatioPerustietoV4> search(OrganisaatioSearchCriteriaDTOV4 hakuEhdot) {
        SearchCriteria searchCriteria = searchCriteriaService.getServiceSearchCriteria(hakuEhdot);
        SearchConfig searchConfig = new SearchConfig(false, false, false);
        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessService.findBy(searchCriteria, searchConfig);
        return HakuTulos.<OrganisaatioPerustietoV4>builder().numHits(organisaatiot.size()).items(organisaatiot.stream()
                .map(a -> organisaatioDTOV4ModelMapper.map(a, OrganisaatioPerustietoV4.class))
                .sorted(Comparator.comparing(OrganisaatioPerustietoV4::getOid))
                .collect(Collectors.toCollection(LinkedHashSet::new))).build();
    }
}
