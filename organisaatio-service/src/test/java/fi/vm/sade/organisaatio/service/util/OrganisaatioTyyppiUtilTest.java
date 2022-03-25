package fi.vm.sade.organisaatio.service.util;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioTyyppiUtilTest {

    @Test
    public void getOrgTypeLimit() {
        Arrays.stream(OrganisaatioTyyppi.values()).forEach(this::getOrgTypeLimit);
    }

    private void getOrgTypeLimit(OrganisaatioTyyppi organisaatioTyyppi) {
        assertThat(OrganisaatioTyyppiUtil.getOrgTypeLimit(organisaatioTyyppi.koodiValue()))
                .withFailMessage("getOrgTypeLimit(%s) palauttaa NULL", organisaatioTyyppi)
                .isNotNull();
    }

}
