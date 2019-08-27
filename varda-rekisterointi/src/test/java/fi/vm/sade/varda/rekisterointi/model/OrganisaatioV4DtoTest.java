package fi.vm.sade.varda.rekisterointi.model;

import fi.vm.sade.suomifi.valtuudet.OrganisationDto;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisaatioV4DtoTest {

    @Test
    public void ofOrganisationDto() {
        OrganisationDto dto = new OrganisationDto();
        dto.identifier = "1234-5";
        dto.name = "name 123";

        OrganisaatioV4Dto organisaatio = OrganisaatioV4Dto.of(dto);

        assertThat(organisaatio).returns("1234-5", o -> o.ytunnus);
    }

}
