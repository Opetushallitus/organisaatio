package fi.vm.sade.organisaatio.model;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.util.*;

/**
 * LOP related metadata. Always accosiated with a Organisation.
 *
 * @author mlyly
 */
@Entity
@Table(name="organisaatiometadata")
@Comment("Sisältää organisaation metatiedot, kuten nimi ja kuva.")
public class OrganisaatioMetaData extends BaseEntity {

    /**
     * Owner organisation
     */
    @OneToOne(mappedBy = "metadata")
    private Organisaatio organisation;

    /**
     * Translatable name for this LOP.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    private MonikielinenTeksti nimi;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hakutoimistoectsemailmkt")
    private MonikielinenTeksti hakutoimistoEctsEmailmkt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hakutoimistoectsnimimkt")
    private MonikielinenTeksti hakutoimistoEctsNimimkt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hakutoimistoectstehtavanimikemkt")
    private MonikielinenTeksti hakutoimistoEctsTehtavanimikemkt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hakutoimistoectspuhelinmkt")
    private MonikielinenTeksti hakutoimistoEctsPuhelinmkt;


    @Transient private boolean includeImage = true;

    // Hakutoimisto
    @ManyToOne(cascade = CascadeType.ALL)
    private MonikielinenTeksti hakutoimistoNimi;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @BatchSize(size = 200)
    @JoinTable(name="ORGANISAATIOMETADATA_YHTEYSTIETO")
    private List<Yhteystieto> yhteystiedot = new ArrayList<Yhteystieto>();

    /**
     * Code(?) for this LOP.
     */
    private String koodi;

    /**
     * Translated values for this entry by key.
     */
    @OneToMany(cascade = CascadeType.ALL)
    @BatchSize(size = 500)
    @JoinTable(name="ORGANISAATIOMETADATA_NAMEDMONIKIELINENTEKSTI")
    private Set<NamedMonikielinenTeksti> values = new HashSet<NamedMonikielinenTeksti>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false, name="luontipvm")
    private Date luontiPvm = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name="muokkauspvm")
    private Date muokkausPvm = new Date();


    //
    // Getters setters with "logic"
    //

    /**
     * Creates a keyed <code>dataKey</code> MonikielinenTeksti (translations)
     * entry.
     *
     * @param dataKey
     *            Used key, for example "genericInformationAboutCostOfLiving".
     * @param languageCode
     *            Koodisto language code (uri: EN 123) or just some language key
     * @param value
     *            actual textual koodiValue, null koodiValue clears data
     */
    public void setNamedValue(String dataKey, String languageCode, String value) {
        // Find correct entry
        NamedMonikielinenTeksti nmkt = findByKey(dataKey);

        // Create new if not exists
        if (nmkt == null) {
            if (value == null) {
                return;
            }
            nmkt = new NamedMonikielinenTeksti();
            nmkt.setKey(dataKey);
        }

        // Trigger update
        getValues().remove(nmkt);
        getValues().add(nmkt);

        // Add/set translatable text
        if (nmkt.getValue() == null) {
            MonikielinenTeksti mt = new MonikielinenTeksti();
            nmkt.setValue(mt);
        }

        // Create/update the actual koodiValue with selected language
        if (value != null) {
            nmkt.getValue().addString(languageCode, value);
        } else {
            nmkt.getValue().getValues().remove(languageCode);
        }
    }

    /**
     * Get correct translated text with all languages.
     *
     * @param dataKey
     * @return
     */
    public MonikielinenTeksti getNamedValue(String dataKey) {
        NamedMonikielinenTeksti t = findByKey(dataKey);
        return t == null ? null : t.getValue();
    }

    /**
     * Get named koodiValue for given language.
     *
     * @param dataKey
     * @param languageCode
     * @return
     */
    public String getNamedValue(String dataKey, String languageCode) {
        MonikielinenTeksti t = getNamedValue(dataKey);
        return t == null ? null : t.getString(languageCode);
    }

    /*
     * Find named entry.
     */
    private NamedMonikielinenTeksti findByKey(String key) {
        for (NamedMonikielinenTeksti namedMonikielinenTeksti : values) {
            if (namedMonikielinenTeksti.getKey().equals(key)) {
                return namedMonikielinenTeksti;
            }
        }
        return null;
    }

    public void setNimi(String languageCode, String value) {
        if (getNimi() == null) {
            setNimi(new MonikielinenTeksti());
        }
        getNimi().addString(languageCode, value);
    }

    public String getNimi(String languageCode) {
        return getNimi() == null ? null : getNimi().getString(languageCode);
    }


    //
    // Getters setters
    //

    public List<Yhteystieto> getYhteystiedot() {
        return yhteystiedot;
    }

    public MonikielinenTeksti getHakutoimistoNimi() {
        return hakutoimistoNimi;
    }

    public void setHakutoimistoNimi(MonikielinenTeksti hakutoimistoNimi) {
        this.hakutoimistoNimi = hakutoimistoNimi;
    }

    public MonikielinenTeksti getHakutoimistoEctsNimi() {
        return hakutoimistoEctsNimimkt;
    }

    public void setHakutoimistoEctsNimi(MonikielinenTeksti hakutoimistoEctsNimi) {
        this.hakutoimistoEctsNimimkt = hakutoimistoEctsNimi;
    }

    public MonikielinenTeksti getHakutoimistoEctsTehtavanimike() {
        return hakutoimistoEctsTehtavanimikemkt;
    }

    public void setHakutoimistoEctsTehtavanimike(MonikielinenTeksti hakutoimistoEctsTehtavanimike) {
        this.hakutoimistoEctsTehtavanimikemkt = hakutoimistoEctsTehtavanimike;
    }

    public MonikielinenTeksti getHakutoimistoEctsEmail() {
        return hakutoimistoEctsEmailmkt;
    }

    public void setHakutoimistoEctsEmail(MonikielinenTeksti hakutoimistoEctsSahkoposti) {
        this.hakutoimistoEctsEmailmkt = hakutoimistoEctsSahkoposti;
    }

    public MonikielinenTeksti getHakutoimistoEctsPuhelin() {
        return hakutoimistoEctsPuhelinmkt;
    }

    public void setHakutoimistoEctsPuhelin(MonikielinenTeksti hakutoimistoEctsPuhelin) {
        this.hakutoimistoEctsPuhelinmkt = hakutoimistoEctsPuhelin;
    }


    public Organisaatio getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisaatio organisation) {
        this.organisation = organisation;
    }

    public MonikielinenTeksti getNimi() {
        return nimi;
    }

    public void setNimi(MonikielinenTeksti nimi) {
        this.nimi = nimi;
    }

    public String getKoodi() {
        return koodi;
    }

    public void setKoodi(String koodi) {
        this.koodi = koodi;
    }

    public Set<NamedMonikielinenTeksti> getValues() {
        return values;
    }

    public void setValues(Set<NamedMonikielinenTeksti> values) {
        this.values = values;
    }

    public Date getLuontiPvm() {
        return luontiPvm;
    }

    public void setLuontiPvm(Date luontiPvm) {
        this.luontiPvm = luontiPvm;
    }

    public Date getMuokkausPvm() {
        return muokkausPvm;
    }

    public void setMuokkausPvm(Date muokkausPvm) {
        this.muokkausPvm = muokkausPvm;
    }

    /**
     * @return the includeImage
     */
    public boolean isIncludeImage() {
        return includeImage;
    }

    /**
     * @param includeImage the includeImage to set
     */
    public void setIncludeImage(boolean includeImage) {
        this.includeImage = includeImage;
    }
}
