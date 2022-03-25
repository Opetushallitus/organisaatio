package fi.vm.sade.organisaatio.dto.mapping;

import fi.vm.sade.organisaatio.api.search.OrganisaatioSearchCriteria;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchCriteriaModelMapperTest {

    private SearchCriteriaModelMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new SearchCriteriaModelMapper();
    }

    @Test
    public void organisaatioSearchCriteriaDTOV2OrganisaatioTyyppi() {
        OrganisaatioSearchCriteriaDTOV2 dto = new OrganisaatioSearchCriteriaDTOV2();

        dto.setOrganisaatiotyyppi("Koulutustoimija");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).containsExactly("organisaatiotyyppi_01");

        dto.setOrganisaatiotyyppi(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();

        dto.setOrganisaatiotyyppi("Ryhma");
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

        dto.setOrganisaatiotyyppi("Koulutustoimija");
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).containsExactly("organisaatiotyyppi_01");

        dto.setOrganisaatiotyyppi(null);
        assertThat(mapper.map(dto, SearchCriteria.class).getOrganisaatioTyyppi()).isEmpty();

        dto.setOrganisaatiotyyppi("Ryhma");
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
