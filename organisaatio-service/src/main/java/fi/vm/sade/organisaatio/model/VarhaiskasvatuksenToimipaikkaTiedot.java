package fi.vm.sade.organisaatio.model;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Sisältää varhaiskasvatuksen toimipaikka -tyyppisen organisaation vaatimat tiedot jolle tämä on pakollinen.
 */
@Entity
@Table(name = "varhaiskasvatuksen_toimipaikka_tiedot")
public class VarhaiskasvatuksenToimipaikkaTiedot extends BaseEntity {

    // Koodisto vardajarjestamismuoto
    @Column(name = "toimintamuoto", nullable = false)
    private String toimintamuoto;

    // Koodisto vardakasvatusopillinenjarjestelma
    @Column(name = "kasvatusopillinen_jarjestelma", nullable = false)
    private String kasvatusopillinenJarjestelma;

    // Koodisto vardatoimintamuoto
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "varhaiskasvatuksen_jarjestamismuoto", joinColumns = @JoinColumn(name = "varhaiskasvatuksen_toimipaikka_tiedot_id"))
    @Column(name = "toimintamuoto", nullable = false)
    private Set<String> varhaiskasvatuksenJarjestamismuodot = new HashSet<>();

    @Column(name = "paikkojen_lukumaara", nullable = false)
    private Long paikkojenLukumaara;

    // Koodisto maatjavaltiot2
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "varhaiskasvatuksenToimipaikkaTiedot")
    @BatchSize(size = 100)
    private Set<VarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotukset = new HashSet<>();

    // Koodisto vardatoiminnallinenpainotus
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "varhaiskasvatuksenToimipaikkaTiedot")
    @BatchSize(size = 100)
    private Set<VarhaiskasvatuksenToiminnallinenpainotus> varhaiskasvatuksenToiminnallinenpainotukset = new HashSet<>();

    public String getToimintamuoto() {
        return toimintamuoto;
    }

    public void setToimintamuoto(String jarjestamismuoto) {
        this.toimintamuoto = jarjestamismuoto;
    }

    public String getKasvatusopillinenJarjestelma() {
        return kasvatusopillinenJarjestelma;
    }

    public void setKasvatusopillinenJarjestelma(String kasvatusopillinenJarjestelma) {
        this.kasvatusopillinenJarjestelma = kasvatusopillinenJarjestelma;
    }

    public Long getPaikkojenLukumaara() {
        return paikkojenLukumaara;
    }

    public void setPaikkojenLukumaara(Long paikkojenLukumaara) {
        this.paikkojenLukumaara = paikkojenLukumaara;
    }

    public Set<VarhaiskasvatuksenKielipainotus> getVarhaiskasvatuksenKielipainotukset() {
        return varhaiskasvatuksenKielipainotukset;
    }

    public void setVarhaiskasvatuksenKielipainotukset(Set<VarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotukset) {
        this.varhaiskasvatuksenKielipainotukset = varhaiskasvatuksenKielipainotukset;
    }

    public Set<VarhaiskasvatuksenToiminnallinenpainotus> getVarhaiskasvatuksenToiminnallinenpainotukset() {
        return varhaiskasvatuksenToiminnallinenpainotukset;
    }

    public void setVarhaiskasvatuksenToiminnallinenpainotukset(Set<VarhaiskasvatuksenToiminnallinenpainotus> varhaiskasvatuksenToimintamuodot) {
        this.varhaiskasvatuksenToiminnallinenpainotukset = varhaiskasvatuksenToimintamuodot;
    }

    public Set<String> getVarhaiskasvatuksenJarjestamismuodot() {
        return varhaiskasvatuksenJarjestamismuodot;
    }

    public void setVarhaiskasvatuksenJarjestamismuodot(Set<String> toimintamuoto) {
        this.varhaiskasvatuksenJarjestamismuodot = toimintamuoto;
    }
}
