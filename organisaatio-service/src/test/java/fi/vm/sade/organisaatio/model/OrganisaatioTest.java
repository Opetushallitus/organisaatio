package fi.vm.sade.organisaatio.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioTest {

    @Test
    public void parentOidsWithNull() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(null);

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    public void parentOidsWithEmpty() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Collections.emptyList());

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    public void parentOidWithValidPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Arrays.asList(
                "1.2.246.562.10.86638002385", "1.2.246.562.10.81269623245", "1.2.246.562.10.00000000001"));

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.86638002385");
    }

    @Test
    public void parentOidWithValidRootPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOids(Collections.singletonList("1.2.246.562.10.00000000001"));

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.00000000001");
    }

}
