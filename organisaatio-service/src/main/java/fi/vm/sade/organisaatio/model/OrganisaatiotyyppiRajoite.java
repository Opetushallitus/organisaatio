package fi.vm.sade.organisaatio.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static fi.vm.sade.organisaatio.dto.Rajoitetyyppi.CONSTANTS.ORGANISAATIOTYYPPI_CONSTANT;

@Entity
@DiscriminatorValue(ORGANISAATIOTYYPPI_CONSTANT)
public class OrganisaatiotyyppiRajoite extends Rajoite {
}
