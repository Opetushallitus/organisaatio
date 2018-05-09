package fi.vm.sade.organisaatio.model;

import fi.vm.sade.generic.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lisatietotyyppi")
public class Lisatietotyyppi extends BaseEntity {
    // Lokalisointipalvelun avain
    @Column(name = "nimi", unique = true)
    private String nimi;

    @OneToMany(mappedBy = "lisatietotyyppi")
    private Set<Rajoite> rajoitteet = new HashSet<>();

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

}
