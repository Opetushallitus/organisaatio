package fi.vm.sade.organisaatio.resource.impl.v3;

import fi.vm.sade.organisaatio.dto.v3.OrganisaatioRDTOV3;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.organisaatio.resource.dto.ResultRDTO;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrganisaatioResourceImplV3Test {

    @InjectMocks
    private OrganisaatioResourceImplV3 organisaatioResourceImplV3;

    @Mock
    private OrganisaatioResource organisaatioResource;

    @Test
    public void updateOrganisaatioShouldMapYhteystiedotToDtoV1() {
        when(organisaatioResource.updateOrganisaatio(any(), any())).thenAnswer((InvocationOnMock invocation)
                -> new ResultRDTO(invocation.getArgumentAt(1, OrganisaatioRDTO.class)));
        OrganisaatioRDTOV3 dtov3 = new OrganisaatioRDTOV3();
        dtov3.setYhteystiedot(singletonList(Maps.newHashMap("fi", "suomeksi")));

        organisaatioResourceImplV3.updateOrganisaatio("oid1", dtov3);

        ArgumentCaptor<OrganisaatioRDTO> argumentCaptor = ArgumentCaptor.forClass(OrganisaatioRDTO.class);
        verify(organisaatioResource).updateOrganisaatio(any(), argumentCaptor.capture());
        OrganisaatioRDTO dtov1 = argumentCaptor.getValue();
        assertThat(dtov1.getYhteystiedot()).containsExactly(Maps.newHashMap("fi", "suomeksi"));
    }

}
