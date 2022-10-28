package fi.vm.sade.organisaatio.auth;

import fi.vm.sade.organisaatio.business.exception.NotAuthorizedException;
import fi.vm.sade.organisaatio.repository.OrganisaatioRepository;
import fi.vm.sade.organisaatio.dto.v4.OrganisaatioRDTOV4;
import fi.vm.sade.organisaatio.model.MonikielinenTeksti;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@SpringBootTest
@AutoConfigureTestDatabase
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
        when(organisaatioRepository.findFirstByOid(any())).thenReturn(entity);

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
        when(organisaatioRepository.findFirstByOid(any())).thenReturn(entity);

        OrganisaatioRDTOV4 dto = new OrganisaatioRDTOV4();
        dto.setOid("oid");
        dto.setNimi(singletonMap("fi", "nimi suomeksi"));

        Throwable throwable = catchThrowable(() -> permissionChecker.checkSaveOrganisation(dto, true));

        assertThat(throwable).isInstanceOf(NotAuthorizedException.class);
        verify(organisaatioPermissionServiceImpl).userCanUpdateOrganisation(any());
        verifyNoMoreInteractions(organisaatioPermissionServiceImpl);
    }

}
