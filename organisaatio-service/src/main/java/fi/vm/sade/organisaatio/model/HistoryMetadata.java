package fi.vm.sade.organisaatio.model;

import javax.persistence.*;

import fi.vm.sade.security.xssfilter.FilterXss;
import fi.vm.sade.security.xssfilter.XssFilterListener;

import java.util.Date;

/**
 * Timeline metadata. Used for example to store old and planned Organisation name changes.
 *
 *
 * @author mlyly
 * @see fi.vm.sade.organisaatio.dao.OrganisaatioDAOImpl#addHistoriaMetadata(Long, String, String, java.util.Date, String)
 */
@Entity
@Table(name = "history_metadata",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"organisaatio_id", "avain", "kieli", "aika"})}
)
@EntityListeners(XssFilterListener.class)
public class HistoryMetadata extends OrganisaatioBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Organisaatio organisaatio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    Date aika = new Date();

    @Column(nullable = false)
    private String avain;

    @Column(nullable = false)
    private String kieli;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    @FilterXss
    private String arvo;

    public Organisaatio getOrganisaatio() {
        return organisaatio;
    }

    public void setOrganisaatio(Organisaatio organisaatio) {
        this.organisaatio = organisaatio;
    }

    public Date getAika() {
        return aika;
    }

    public void setAika(Date aika) {
        this.aika = aika;
    }

    public String getAvain() {
        return avain;
    }

    public void setAvain(String avain) {
        this.avain = avain;
    }

    public String getKieli() {
        return kieli;
    }

    public void setKieli(String kieli) {
        this.kieli = kieli;
    }

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }
}
