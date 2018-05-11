package fi.vm.sade.organisaatio.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("oppilaitostyyppi")
public class OppilaitostyyppiRajoite extends Rajoite {
    // oppilaitostyyppi-koodisto arvo
}
