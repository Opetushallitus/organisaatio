package fi.vm.sade.organisaatio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "organisaatio_lisatieto")
public class OrganisaatioLisatietotyyppi extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Organisaatio organisaatio;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lisatietotyyppi lisatietotyyppi;


    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    public Lisatietotyyppi getLisatietotyyppi() {
        return lisatietotyyppi;
    }

    public void setLisatietotyyppi(Lisatietotyyppi lisatietotyyppi) {
        this.lisatietotyyppi = lisatietotyyppi;
    }
}
