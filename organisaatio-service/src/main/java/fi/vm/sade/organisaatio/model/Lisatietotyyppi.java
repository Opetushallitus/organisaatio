package fi.vm.sade.organisaatio.model;

import fi.vm.sade.generic.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "lisatietotyyppi")
public class Lisatietotyyppi extends BaseEntity {
    // Lokalisointipalvelun avain
    @Column(name = "nimi")
    private String nimi;

    @Column(name = "organisaatiotyyppi_rajoite")
    private String organisaatioTyyppiRajoite;

    // oppilaitostyyppi-koodisto
    @Column(name = "oppilaitostyyppi_rajoite")
    private String oppilaitosTyyppiRajoite;


    public String getNimi() {
        return nimi;
    }

    public void setNimi(String koodiUri) {
        this.nimi = koodiUri;
    }

    public String getOrganisaatioTyyppiRajoite() {
        return organisaatioTyyppiRajoite;
    }

    public void setOrganisaatioTyyppiRajoite(String organisaatioTyyppiRajoite) {
        this.organisaatioTyyppiRajoite = organisaatioTyyppiRajoite;
    }

    public String getOppilaitosTyyppiRajoite() {
        return oppilaitosTyyppiRajoite;
    }

    public void setOppilaitosTyyppiRajoite(String oppilaitosTyyppiRajoite) {
        this.oppilaitosTyyppiRajoite = oppilaitosTyyppiRajoite;
    }
}
