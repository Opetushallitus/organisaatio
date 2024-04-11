package fi.vm.sade.organisaatio.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Organisaatiolle (tai sen virkailijoille) lähetetty sähköposti.
 */
@Entity
@Table(name = "organisaatio_sahkoposti")
public class OrganisaatioSahkoposti extends BaseEntity {

    public enum Tyyppi {
        VANHENTUNEET_TIEDOT,
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisaatio_id", nullable = false,
            foreignKey = @ForeignKey(name = "organisaatio_sahkoposti_organisaatio_id_fkey"))
    private Organisaatio organisaatio;

    @Column(name = "tyyppi", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Tyyppi tyyppi;

    @Column(name = "aikaleima", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date aikaleima;

    @Column(name = "viestintapalvelu_id")
    private String viestintapalveluId;

    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    public Tyyppi getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(Tyyppi tyyppi) {
        this.tyyppi = tyyppi;
    }

    public Date getAikaleima() {
        return aikaleima;
    }

    public void setAikaleima(Date aikaleima) {
        this.aikaleima = aikaleima;
    }

    public String getViestintapalveluId() {
        return viestintapalveluId;
    }

    public void setViestintapalveluId(String viestintapalveluId) {
        this.viestintapalveluId = viestintapalveluId;
    }
}
