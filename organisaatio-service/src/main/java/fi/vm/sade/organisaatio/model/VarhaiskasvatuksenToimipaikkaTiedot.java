package fi.vm.sade.organisaatio.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Sisältää varhaiskasvatuksen toimipaikka -tyyppisen organisaation vaatimat tiedot jolle tämä on pakollinen.
 */
@Entity
@Table(name = "varhaiskasvatuksen_toimipaikka_tiedot")
public class VarhaiskasvatuksenToimipaikkaTiedot extends BaseEntity {

    // Koodisto vardajarjestamismuoto
    @Column(name = "jarjestamismuoto", nullable = false)
    private String jarjestamismuoto;

    // Koodisto vardakasvatusopillinenjarjestelma
    @Column(name = "kasvatusopillinen_jarjestelma", nullable = false)
    private String kasvatusopillinenJarjestelma;

    // Koodisto vardatoiminnallinenpainotus
    @Column(name = "toiminnallinen_painotus", nullable = false)
    private String toiminnallinenPainotus;

    @Column(name = "paikkojen_lukumaara", nullable = false)
    private Long paikkojenLukumaara;

    // Koodisto maatjavaltiot2
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "varhaiskasvatuksenToimipaikkaTiedot")
    private Set<VarhaiskasvatuksenKielipainotus> varhaiskasvatuksenKielipainotukset = new HashSet<>();

    // Koodisto vardatoimintamuoto
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "varhaiskasvatuksenToimipaikkaTiedot")
    private Set<VarhaiskasvatuksenToimintamuoto> varhaiskasvatuksenToimintamuodot = new HashSet<>();

    public String getJarjestamismuoto() {
        return jarjestamismuoto;
    }

    public void setJarjestamismuoto(String jarjestamismuoto) {
        this.jarjestamismuoto = jarjestamismuoto;
    }

    public String getKasvatusopillinenJarjestelma() {
        return kasvatusopillinenJarjestelma;
    }

    public void setKasvatusopillinenJarjestelma(String kasvatusopillinenJarjestelma) {
        this.kasvatusopillinenJarjestelma = kasvatusopillinenJarjestelma;
    }

    public String getToiminnallinenPainotus() {
        return toiminnallinenPainotus;
    }

    public void setToiminnallinenPainotus(String toiminnallinenPainotus) {
        this.toiminnallinenPainotus = toiminnallinenPainotus;
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

    public Set<VarhaiskasvatuksenToimintamuoto> getVarhaiskasvatuksenToimintamuodot() {
        return varhaiskasvatuksenToimintamuodot;
    }

    public void setVarhaiskasvatuksenToimintamuodot(Set<VarhaiskasvatuksenToimintamuoto> varhaiskasvatuksenToimintamuodot) {
        this.varhaiskasvatuksenToimintamuodot = varhaiskasvatuksenToimintamuodot;
    }
}
