package fi.vm.sade.organisaatio.auth;

import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepositoryCustom;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class PermissionCheckerTest {

    @InjectMocks
    private PermissionChecker permissionChecker;

    @Mock
    private OrganisaatioRepository organisaatioRepository;
    @Mock
    private OrganisaatioPermissionServiceImpl organisaatioPermissionServiceImpl;

    @Test
    public void checkSaveOrganisationWithNameChange() {
        Organisaatio entity = new Organisaatio();
        entity.setOid("oid");
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("fi", "nimi suomeksi"));
        entity.setNimi(nimi);
        when(organisaatioRepository.customFindByOid(any())).thenReturn(entity);

        OrganisaatioRDTOV4 dto = new OrganisaatioRDTOV4();
        dto.setOid("oid");
        dto.setNimi(singletonMap("fi", "nimi suomeksi muutettu"));

        Throwable throwable = catchThrowable(() -> permissionChecker.checkSaveOrganisation(dto, true));

        assertThat(throwable).isInstanceOf(NotAuthorizedException.class);
        verify(organisaatioPermissionServiceImpl).userCanEditName(any());
        verifyNoMoreInteractions(organisaatioPermissionServiceImpl);
    }

    @Test
    public void checkSaveOrganisationWithoutNameChange() {
        Organisaatio entity = new Organisaatio();
        entity.setOid("oid");
        MonikielinenTeksti nimi = new MonikielinenTeksti();
        nimi.setValues(singletonMap("fi", "nimi suomeksi"));
        entity.setNimi(nimi);
        when(organisaatioRepository.customFindByOid(any())).thenReturn(entity);

        OrganisaatioRDTOV4 dto = new OrganisaatioRDTOV4();
        dto.setOid("oid");
        dto.setNimi(singletonMap("fi", "nimi suomeksi"));

        Throwable throwable = catchThrowable(() -> permissionChecker.checkSaveOrganisation(dto, true));

        assertThat(throwable).isInstanceOf(NotAuthorizedException.class);
        verify(organisaatioPermissionServiceImpl).userCanUpdateOrganisation(any());
        verifyNoMoreInteractions(organisaatioPermissionServiceImpl);
    }

}
