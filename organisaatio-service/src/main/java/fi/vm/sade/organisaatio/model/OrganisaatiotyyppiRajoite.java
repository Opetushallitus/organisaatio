package fi.vm.sade.organisaatio.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import static fi.vm.sade.organisaatio.dto.Rajoitetyyppi.CONSTANTS.ORGANISAATIOTYYPPI_CONSTANT;

@Entity
@DiscriminatorValue(ORGANISAATIOTYYPPI_CONSTANT)
public class OrganisaatiotyyppiRajoite extends Rajoite {
}
