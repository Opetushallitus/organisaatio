package fi.vm.sade.security;

import fi.vm.sade.organisaatio.business.OrganisaatioFindBusinessService;
import fi.vm.sade.organisaatio.model.Organisaatio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class OidProviderTest {

    @Mock
    private OrganisaatioFindBusinessService organisaatioFindBusinessService;

    @Test
    void test1() {

        OidProvider instance = new OidProvider();
        ReflectionTestUtils.setField(instance, "rootOrganisaatioOid", "rootDef");
        ReflectionTestUtils.setField(instance, "organisaatioFindBusinessService", organisaatioFindBusinessService);
        when(organisaatioFindBusinessService.findById(any(String.class))).thenAnswer(foo -> {
            Organisaatio org = new Organisaatio();
            org.setParentOids(Arrays.asList("root","foo"));
            return org;
        });
        assertThat(instance.getSelfAndParentOids("bar").stream().collect(Collectors.joining("/"))).isEqualTo("root/foo/bar");

        when(organisaatioFindBusinessService.findById(any(String.class))).thenAnswer(foo -> {
            Organisaatio org = new Organisaatio();
            return org;
        });
        assertThat(instance.getSelfAndParentOids("bar").stream().collect(Collectors.joining("/"))).isEqualTo("bar");

        when(organisaatioFindBusinessService.findById(any(String.class))).thenAnswer(foo -> null);
        assertThat(instance.getSelfAndParentOids("bar").stream().collect(Collectors.joining("/"))).isEqualTo("rootDef/bar");
    }

}