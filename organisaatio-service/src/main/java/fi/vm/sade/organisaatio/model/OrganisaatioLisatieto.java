package fi.vm.sade.organisaatio.model;

import fi.vm.sade.generic.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organisaatio_lisatieto")
public class OrganisaatioLisatieto extends BaseEntity {
    @ManyToOne
    private Organisaatio organisaatio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lisatietotyyppi_id")
    private Lisatietotyyppi lisatietotyyppi;

    private String arvo;

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

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }
}
