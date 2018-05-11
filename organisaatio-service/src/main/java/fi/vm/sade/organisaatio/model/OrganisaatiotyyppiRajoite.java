package fi.vm.sade.organisaatio.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("organisaatiotyyppi")
public class OrganisaatiotyyppiRajoite extends Rajoite {
}
