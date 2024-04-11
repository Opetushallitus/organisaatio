package fi.vm.sade.organisaatio.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import static fi.vm.sade.organisaatio.dto.Rajoitetyyppi.CONSTANTS.OPPILAITOSTYYPPI_CONSTANT;

@Entity
@DiscriminatorValue(OPPILAITOSTYYPPI_CONSTANT)
public class OppilaitostyyppiRajoite extends Rajoite {
    // oppilaitostyyppi-koodisto arvo
}
