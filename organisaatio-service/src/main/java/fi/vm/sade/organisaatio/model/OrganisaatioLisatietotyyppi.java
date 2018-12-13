package fi.vm.sade.organisaatio.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
