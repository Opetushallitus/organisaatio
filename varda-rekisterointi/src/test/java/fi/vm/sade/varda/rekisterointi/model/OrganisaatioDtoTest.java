package fi.vm.sade.varda.rekisterointi.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioDtoTest {

    @Test
    public void ofOrganisationDto() {
        OrganisaatioDto organisaatio = OrganisaatioDto.of("1234-5", "name 123");

        assertThat(organisaatio).returns("1234-5", o -> o.ytunnus);
    }

}
