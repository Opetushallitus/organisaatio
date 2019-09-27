package fi.vm.sade.varda.rekisterointi.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioV4DtoTest {

    @Test
    public void ofOrganisationDto() {
        OrganisaatioV4Dto organisaatio = OrganisaatioV4Dto.of("1234-5", "name 123");

        assertThat(organisaatio).returns("1234-5", o -> o.ytunnus);
    }

}
