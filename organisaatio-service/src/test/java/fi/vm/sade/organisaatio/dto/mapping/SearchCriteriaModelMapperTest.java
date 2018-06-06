package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SearchCriteriaModelMapperTest {

    private SearchCriteriaModelMapper mapper;

    @Before
    public void setup() {
        mapper = new SearchCriteriaModelMapper();
    }

    @Test
    public void organisaatioSearchCriteriaDTOV2OrganisaatioTyyppi() {
        OrganisaatioSearchCriteriaDTOV2 dto = new OrganisaatioSearchCriteriaDTOV2();

        dto.setOrganisaatioTyyppi("tyyppi1");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).containsExactly("tyyppi1");

        dto.setOrganisaatioTyyppi(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();

        dto.setOrganisaatioTyyppi("Ryhma");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();
    }

    @Test
    public void organisaatioSearchCriteriaDTOV2Oid() {
        OrganisaatioSearchCriteriaDTOV2 dto = new OrganisaatioSearchCriteriaDTOV2();

        dto.setOid("oid1");
        assertThat(mapper.map(dto, SearchCriteria.class).getOid()).containsExactly("oid1");

        dto.setOid(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOid()).isEmpty();
    }

    @Test
    public void organisaatioSearchCriteriaOrganisaatioTyyppi() {
        OrganisaatioSearchCriteria dto = new OrganisaatioSearchCriteria();

        dto.setOrganisaatioTyyppi("tyyppi1");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).containsExactly("tyyppi1");

        dto.setOrganisaatioTyyppi(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();

        dto.setOrganisaatioTyyppi("Ryhma");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();
    }

    @Test
    public void organisaatioSearchCriteriaOid() {
        OrganisaatioSearchCriteria dto = new OrganisaatioSearchCriteria();

        dto.setOid("oid1");
        assertThat(mapper.map(dto, SearchCriteria.class).getOid()).containsExactly("oid1");

        dto.setOid(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOid()).isEmpty();
    }

}
