package fi.vm.sade.organisaatio.business.impl;

import fi.vm.sade.organisaatio.OrganisaatioBuilder;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.auth.PermissionChecker;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioSuhdeRepository;
import fi.vm.sade.organisaatio.service.TimeService;
import fi.vm.sade.organisaatio.service.search.SearchConfig;
import fi.vm.sade.organisaatio.service.search.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
class OrganisaatioFindBusinessServiceImplTest {

    @Mock
    private OrganisaatioRepository organisaatioDaoMock;

    @Mock
    private OrganisaatioSuhdeRepository organisaatioSuhdeRepositoryMock;

    @Mock
    private ConversionService conversionServiceMock;

    @Mock
    private TimeService timeServiceMock;

    @Mock
    private PermissionChecker permissionChecker;

    @InjectMocks
    private OrganisaatioFindBusinessServiceImpl organisaatioFindBusinessServiceImpl;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(organisaatioFindBusinessServiceImpl, "rootOrganisaatioOid", "rootOid");
        when(conversionServiceMock.convert(any(Organisaatio.class), eq(OrganisaatioPerustieto.class))).thenAnswer(invocation -> {
            Organisaatio entity = invocation.getArgument(0, Organisaatio.class);
            OrganisaatioPerustieto dto = new OrganisaatioPerustieto();
            dto.setOid(entity.getOid());
            return dto;
        });
    }

    @Test
    void findBy() {
        SearchCriteria criteria = new SearchCriteria();
        SearchConfig config = new SearchConfig(true, false, false);
        Organisaatio rootOrganisaatio = new OrganisaatioBuilder("rootOid").build();
        Organisaatio organisaatio1 = new OrganisaatioBuilder("oid1").parent(rootOrganisaatio).build();
        Organisaatio organisaatio2 = new OrganisaatioBuilder("oid2").parent(organisaatio1).build();
        Organisaatio organisaatio3 = new OrganisaatioBuilder("oid3").parent(organisaatio1).build();
        when(organisaatioDaoMock.findBy(eq(criteria), any())).thenReturn(asList(organisaatio1, organisaatio2, organisaatio3));

        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessServiceImpl.findBy(criteria, config);

        assertThat(organisaatiot).extracting(OrganisaatioPerustieto::getOid).containsExactlyInAnyOrder("oid1", "oid2", "oid3");
        ArgumentCaptor<SearchCriteria> searchCriteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(organisaatioDaoMock).findBy(searchCriteriaCaptor.capture(), any());
        SearchCriteria searchCriteria = searchCriteriaCaptor.getValue();
        assertThat(searchCriteria).isSameAs(criteria);
        verify(organisaatioDaoMock, never()).countActiveChildrenByOid(any());
    }

    @Test
    void findByIncludeParents() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setSearchStr("foo");
        SearchConfig config = new SearchConfig(true, false, false);
        Organisaatio rootOrganisaatio = new OrganisaatioBuilder("rootOid").build();
        Organisaatio organisaatio1 = new OrganisaatioBuilder("oid1").parent(rootOrganisaatio).build();
        Organisaatio organisaatio2 = new OrganisaatioBuilder("oid2").parent(rootOrganisaatio).build();
        Organisaatio organisaatio3 = new OrganisaatioBuilder("oid3").parent(organisaatio2).build();
        when(organisaatioDaoMock.findBy(eq(criteria), any())).thenReturn(asList(organisaatio1, organisaatio3));

        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessServiceImpl.findBy(criteria, config);

        assertThat(organisaatiot).extracting(OrganisaatioPerustieto::getOid).containsExactlyInAnyOrder("oid1", "oid3");
        ArgumentCaptor<SearchCriteria> searchCriteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(organisaatioDaoMock, times(2)).findBy(searchCriteriaCaptor.capture(), any());
        List<SearchCriteria> searchCriterias = searchCriteriaCaptor.getAllValues();
        assertThat(searchCriterias.get(0)).isSameAs(criteria);
        assertThat(searchCriterias.get(1).getOid()).containsExactlyInAnyOrder("rootOid", "oid2");
        verify(organisaatioDaoMock, never()).countActiveChildrenByOid(any());
    }

    @Test
    void findByIncludeChildren() {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setSearchStr("foo");
        SearchConfig config = new SearchConfig(false, true, false);
        Organisaatio rootOrganisaatio = new OrganisaatioBuilder("rootOid").build();
        Organisaatio organisaatio1 = new OrganisaatioBuilder("oid1").parent(rootOrganisaatio).build();
        Organisaatio organisaatio2 = new OrganisaatioBuilder("oid2").parent(organisaatio1).build();
        Organisaatio organisaatio3 = new OrganisaatioBuilder("oid3").parent(organisaatio1).build();
        Organisaatio organisaatio4 = new OrganisaatioBuilder("oid4").parent(rootOrganisaatio).build();
        Organisaatio organisaatio5 = new OrganisaatioBuilder("oid5").parent(organisaatio4).build();
        when(organisaatioDaoMock.findBy(eq(criteria), any())).thenReturn(asList(organisaatio1, organisaatio2, organisaatio3, organisaatio4, organisaatio5));

        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessServiceImpl.findBy(criteria, config);

        assertThat(organisaatiot).extracting(OrganisaatioPerustieto::getOid).containsExactlyInAnyOrder("oid1", "oid2", "oid3", "oid4", "oid5");
        ArgumentCaptor<SearchCriteria> searchCriteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(organisaatioDaoMock, times(2)).findBy(searchCriteriaCaptor.capture(), any());
        List<SearchCriteria> searchCriterias = searchCriteriaCaptor.getAllValues();
        assertThat(searchCriterias.get(0)).isSameAs(criteria);
        verify(organisaatioDaoMock, never()).countActiveChildrenByOid(any());
    }

    @Test
    void findByCountChildren() {
        SearchCriteria criteria = new SearchCriteria();
        SearchConfig config = new SearchConfig(true, false, true);
        Organisaatio rootOrganisaatio = new OrganisaatioBuilder("rootOid").build();
        Organisaatio organisaatio1 = new OrganisaatioBuilder("oid1").parent(rootOrganisaatio).build();
        Organisaatio organisaatio2 = new OrganisaatioBuilder("oid2").parent(organisaatio1).build();
        Organisaatio organisaatio3 = new OrganisaatioBuilder("oid3").parent(organisaatio1).build();
        when(organisaatioDaoMock.findBy(eq(criteria), any())).thenReturn(asList(organisaatio1, organisaatio2, organisaatio3));

        List<OrganisaatioPerustieto> organisaatiot = organisaatioFindBusinessServiceImpl.findBy(criteria, config);

        assertThat(organisaatiot).extracting(OrganisaatioPerustieto::getOid).containsExactlyInAnyOrder("oid1", "oid2", "oid3");
        ArgumentCaptor<SearchCriteria> searchCriteriaCaptor = ArgumentCaptor.forClass(SearchCriteria.class);
        verify(organisaatioDaoMock).findBy(searchCriteriaCaptor.capture(), any());
        SearchCriteria searchCriteria = searchCriteriaCaptor.getValue();
        assertThat(searchCriteria).isSameAs(criteria);
        verify(organisaatioDaoMock).countActiveChildrenByOid(any());
    }

    @Test
    void findByOidsV4ExcludesPiilotettu() {
        boolean excludedPiilotettu = invokeFindByOidsV4(false);
        assertThat(excludedPiilotettu).isTrue();
    }

    @Test
    void findByOidsV4IncludesPiilotettuIfReadAccessToAll() {
        boolean excludedPiilotettu = invokeFindByOidsV4(true);
        assertThat(excludedPiilotettu).isFalse();
    }

    private boolean invokeFindByOidsV4(boolean readAccessToAll) {
        when(permissionChecker.isReadAccessToAll()).thenReturn(readAccessToAll);
        ArgumentCaptor<Boolean> excludesPiilotettuCaptor = ArgumentCaptor.forClass(Boolean.class);
        when(organisaatioDaoMock.findByOids(anyCollection(), anyBoolean(), excludesPiilotettuCaptor.capture()))
                .thenReturn(asList(new Organisaatio()));
        organisaatioFindBusinessServiceImpl.findByOidsV4(Collections.singletonList("1.23.456"));
        return excludesPiilotettuCaptor.getValue();
    }

    @Test
    void haeMuutetutExcludesPiilotettu() {
        boolean excludedPiilotettu = invokeFindModifiedSince(false);
        assertThat(excludedPiilotettu).isTrue();
    }

    @Test
    void haeMuutetutIncludesPiilotettuIfReadAccessToAll() {
        boolean excludedPiilotettu = invokeFindModifiedSince(true);
        assertThat(excludedPiilotettu).isFalse();
    }

    private boolean invokeFindModifiedSince(boolean readAccessToAll) {
        when(permissionChecker.isReadAccessToAll()).thenReturn(readAccessToAll);
        ArgumentCaptor<Boolean> excludesPiilotettuCaptor = ArgumentCaptor.forClass(Boolean.class);
        when(organisaatioDaoMock.findModifiedSince(
                    excludesPiilotettuCaptor.capture(),
                    any(LocalDateTime.class),
                    any(List.class),
                    anyBoolean()))
                .thenReturn(asList(new Organisaatio()));
        organisaatioFindBusinessServiceImpl.haeMuutetut(
                LocalDateTime.parse("2010-05-24T00:00:00"), Collections.emptyList(), true);
        return excludesPiilotettuCaptor.getValue();
    }

}
