package fi.vm.sade.organisaatio.service.search;

import fi.vm.sade.organisaatio.auth.OrganisaatioPermissionServiceImpl;
import fi.vm.sade.organisaatio.dto.mapping.OrganisaatioDTOV4ModelMapper;
import fi.vm.sade.organisaatio.dto.mapping.SearchCriteriaModelMapper;
import fi.vm.sade.organisaatio.dto.v2.OrganisaatioSearchCriteriaDTOV2;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioSearchCriteriaDTOV4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchCriteriaServiceTest {

    private SearchCriteriaService service;

    private OrganisaatioPermissionServiceImpl organisaatioPermissionServiceMock;

    @BeforeEach
    public void setup() {
        organisaatioPermissionServiceMock = mock(OrganisaatioPermissionServiceImpl.class);
        service = new SearchCriteriaService(new SearchCriteriaModelMapper(), new OrganisaatioDTOV4ModelMapper(),
                organisaatioPermissionServiceMock);
    }

    @Test
    public void getServiceSearchCriteriaV4SearchStr() {
        OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV4();
        apiSearchCriteria.setSearchStr("hello world");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns("hello world", SearchCriteria::getSearchStr);
    }

    @Test
    public void getServiceSearchCriteriaV4ReadAccessToAllFalse() {
        OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV4();
        apiSearchCriteria.setOid(null);
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns(false, SearchCriteria::getPiilotettu);
        verify(organisaatioPermissionServiceMock, never()).userCanReadOrganisation(any());
    }

    @Test
    public void getServiceSearchCriteriaV4ReadAccessToAllTrue() {
        OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV4();
        apiSearchCriteria.setOid(null);
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(true);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns(null, SearchCriteria::getPiilotettu);
        verify(organisaatioPermissionServiceMock, never()).userCanReadOrganisation(any());
    }

    @Test
    public void getServiceSearchCriteriaV4UserCanReadOrganisationFalse() {
        OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV4();
        apiSearchCriteria.setOid("oid123");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);
        when(organisaatioPermissionServiceMock.userCanReadOrganisation(eq(("oid123")))).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria)
                .returns(false, SearchCriteria::getPiilotettu)
                .returns(singletonList("oid123"), SearchCriteria::getOid);
        verify(organisaatioPermissionServiceMock).userCanReadOrganisation(eq("oid123"));
    }

    @Test
    public void getServiceSearchCriteriaV4UserCanReadOrganisationTrue() {
        OrganisaatioSearchCriteriaDTOV4 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV4();
        apiSearchCriteria.setOid("oid123");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);
        when(organisaatioPermissionServiceMock.userCanReadOrganisation(eq(("oid123")))).thenReturn(true);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria)
                .returns(null, SearchCriteria::getPiilotettu)
                .returns(singletonList("oid123"), SearchCriteria::getOid);
        verify(organisaatioPermissionServiceMock).userCanReadOrganisation(eq("oid123"));
    }

    @Test
    public void getServiceSearchCriteriaV2SearchStr() {
        OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV2();
        apiSearchCriteria.setSearchStr("hello world");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns("hello world", SearchCriteria::getSearchStr);
    }

    @Test
    public void getServiceSearchCriteriaV2ReadAccessToAllFalse() {
        OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV2();
        apiSearchCriteria.setOid(null);
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns(false, SearchCriteria::getPiilotettu);
        verify(organisaatioPermissionServiceMock, never()).userCanReadOrganisation(any());
    }

    @Test
    public void getServiceSearchCriteriaV2ReadAccessToAllTrue() {
        OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV2();
        apiSearchCriteria.setOid(null);
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(true);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria).returns(null, SearchCriteria::getPiilotettu);
        verify(organisaatioPermissionServiceMock, never()).userCanReadOrganisation(any());
    }

    @Test
    public void getServiceSearchCriteriaV2UserCanReadOrganisationFalse() {
        OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV2();
        apiSearchCriteria.setOid("oid123");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);
        when(organisaatioPermissionServiceMock.userCanReadOrganisation(eq(("oid123")))).thenReturn(false);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria)
                .returns(false, SearchCriteria::getPiilotettu)
                .returns(singletonList("oid123"), SearchCriteria::getOid);
        verify(organisaatioPermissionServiceMock).userCanReadOrganisation(eq("oid123"));
    }

    @Test
    public void getServiceSearchCriteriaV2UserCanReadOrganisationTrue() {
        OrganisaatioSearchCriteriaDTOV2 apiSearchCriteria = new OrganisaatioSearchCriteriaDTOV2();
        apiSearchCriteria.setOid("oid123");
        when(organisaatioPermissionServiceMock.isReadAccessToAll()).thenReturn(false);
        when(organisaatioPermissionServiceMock.userCanReadOrganisation(eq(("oid123")))).thenReturn(true);

        SearchCriteria serviceSearchCriteria = service.getServiceSearchCriteria(apiSearchCriteria);

        assertThat(serviceSearchCriteria)
                .returns(null, SearchCriteria::getPiilotettu)
                .returns(singletonList("oid123"), SearchCriteria::getOid);
        verify(organisaatioPermissionServiceMock).userCanReadOrganisation(eq("oid123"));
    }

}
