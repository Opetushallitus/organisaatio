package fi.vm.sade.organisaatio.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static fi.vm.sade.organisaatio.dto.Rajoitetyyppi.CONSTANTS.OPPILAITOSTYYPPI_CONSTANT;

@Entity
@DiscriminatorValue(OPPILAITOSTYYPPI_CONSTANT)
public class OppilaitostyyppiRajoite extends Rajoite {
    // oppilaitostyyppi-koodisto arvo
}
