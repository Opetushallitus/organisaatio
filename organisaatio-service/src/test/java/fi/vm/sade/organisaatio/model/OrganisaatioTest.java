package fi.vm.sade.organisaatio.model;

import org.junit.Ignore;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioTest {

    @Test
    public void parentOidWithNullPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath(null);

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    public void parentOidWithEmptyPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("");

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    public void parentOidWithValidPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("|1.2.246.562.10.00000000001|1.2.246.562.10.81269623245|1.2.246.562.10.86638002385|");

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.86638002385");
    }

    @Test
    public void parentOidWithValidRootPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("|1.2.246.562.10.00000000001|");

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isEqualTo("1.2.246.562.10.00000000001");
    }

    @Test
    public void parentOidWithInvalidPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("1.2.246.562.10.00000000001/1.2.246.562.10.81269623245/1.2.246.562.10.86638002385");

        String parentOid = organisaatio.getParentOid().orElse(null);

        assertThat(parentOid).isNull();
    }

    @Test
    public void parentOidsFromNullPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath(null);

        List<String> parentOids = organisaatio.getParentOidsFromPath();

        assertThat(parentOids).isEmpty();
    }

    @Test
    public void parentOidsFromEmptyPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("");

        List<String> parentOids = organisaatio.getParentOidsFromPath();

        assertThat(parentOids).isEmpty();
    }

    @Test
    @Ignore
    public void parentOidsFromValidPath() {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setParentOidPath("|1.2.246.562.10.00000000001|1.2.246.562.10.81269623245|1.2.246.562.10.86638002385|");

        List<String> parentOids = organisaatio.getParentOidsFromPath();

        assertThat(parentOids).containsExactly("1.2.246.562.10.00000000001", "1.2.246.562.10.81269623245", "1.2.246.562.10.86638002385");
    }

}
