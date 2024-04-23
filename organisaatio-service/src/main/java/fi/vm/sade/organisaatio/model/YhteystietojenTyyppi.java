package fi.vm.sade.organisaatio.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
public class YhteystietojenTyyppi extends OrganisaatioBaseEntity {

	private static final long serialVersionUID = 1L;

	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "nimi_mkt")
    private MonikielinenTeksti nimi;


    @ElementCollection
    @Column(name = "organisaatio_tyyppi")
    @CollectionTable(name = "yhteystietojenTyyppi_organisaatioTyypit", joinColumns = @JoinColumn(name = "yhteystietojenTyyppi_id"))
    private Set<String> sovellettavatOrganisaatioTyyppis = new HashSet<>();

    @ElementCollection
    @Column(name = "oppilaitos_tyyppi")
    @CollectionTable(name = "yhteystietojenTyyppi_oppilaitosTyypit", joinColumns = @JoinColumn(name = "yhteystietojenTyyppi_id"))
    private Set<String> sovellettavatOppilaitostyyppis = new HashSet<>();

    @OneToMany(mappedBy = "yhteystietojenTyyppi", cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<YhteystietoElementti> lisatietos = new HashSet<>();

    @NotNull
    private String oid;

    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    public Set<String> getSovellettavatOrganisaatioTyyppis() {
        return Collections.unmodifiableSet(sovellettavatOrganisaatioTyyppis);
    }

    public void setSovellettavatOrganisaatioTyyppis(Set<String> sovellettavatOrganisaatioTyyppis) {
        this.sovellettavatOrganisaatioTyyppis.clear();
        this.sovellettavatOrganisaatioTyyppis.addAll(sovellettavatOrganisaatioTyyppis);
    }

    public Set<String> getSovellettavatOppilaitostyyppis() {
        return this.sovellettavatOppilaitostyyppis;
    }

    public void setSovellettavatOppilaitostyyppis(
            Set<String> sovellettavatOppilaitostyyppis) {
        this.sovellettavatOppilaitostyyppis = sovellettavatOppilaitostyyppis;
    }

    public Set<YhteystietoElementti> getLisatietos() {
        return Collections.unmodifiableSet(lisatietos);
    }

    public void addLisatieto(YhteystietoElementti lisatieto) {
        lisatieto.setYhteystietojenTyyppi(this);
        lisatietos.add(lisatieto);
    }

    public void setLisatietos(Set<YhteystietoElementti> lisatietos) {

        this.lisatietos.clear();

        for (YhteystietoElementti lisatieto : lisatietos) {

            if (lisatieto != null) {

                addLisatieto(lisatieto);
            }


        }
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
