package fi.vm.sade.organisaatio.model;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lisatietotyyppi")
@BatchSize(size = 100)
public class Lisatietotyyppi extends BaseEntity {
    // Lokalisointipalvelun avain
    @Column(name = "nimi", unique = true)
    private String nimi;

    // Jos on rajoitteita organisaation täytyy täyttää niistä vähintään yksi
    @OneToMany(mappedBy = "lisatietotyyppi", cascade = CascadeType.ALL)
    private Set<Rajoite> rajoitteet = new HashSet<>();

    @OneToMany(mappedBy = "lisatietotyyppi", orphanRemoval = true)
    private Set<OrganisaatioLisatietotyyppi> organisaatioLisatietotyyppis = new HashSet<>();

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String koodiUri) {
        this.nimi = koodiUri;
    }

    public Set<Rajoite> getRajoitteet() {
        return rajoitteet;
    }

    public void setRajoitteet(Set<Rajoite> rajoitteet) {
        this.rajoitteet = rajoitteet;
    }

    public Set<OrganisaatioLisatietotyyppi> getOrganisaatioLisatietotyyppis() {
        return organisaatioLisatietotyyppis;
    }

    public void setOrganisaatioLisatietotyyppis(Set<OrganisaatioLisatietotyyppi> organisaatioLisatietotyyppis) {
        this.organisaatioLisatietotyyppis = organisaatioLisatietotyyppis;
    }
}
