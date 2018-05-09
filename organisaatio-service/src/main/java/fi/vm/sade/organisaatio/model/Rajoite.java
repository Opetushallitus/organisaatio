package fi.vm.sade.organisaatio.model;

import fi.vm.sade.generic.model.BaseEntity;

import javax.persistence.*;

@Entity
@Table(name = "rajoite")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rajoitetyyppi")
public class Rajoite extends BaseEntity {
    @ManyToOne
    private Lisatietotyyppi lisatietotyyppi;

    private String arvo;

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }

    public Lisatietotyyppi getLisatietotyyppi() {
        return lisatietotyyppi;
    }

    public void setLisatietotyyppi(Lisatietotyyppi lisatietotyyppi) {
        this.lisatietotyyppi = lisatietotyyppi;
    }
}
