package fi.vm.sade.organisaatio.model;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 * This class represents a "keyed" translatable text with name and koodiValue - both translatable.
 *
 * Used for LOP's multitude of data... "generalInformationAboutXXX" x 100
 *
 * @author mlyly
 */
@Entity
@BatchSize(size = 200)
public class NamedMonikielinenTeksti extends BaseEntity {

    /**
     * Descriptive "key" - for example "generalInformationAboutStudies"
     */
    private String key;

    /**
     * Translatable name
     */
    @ManyToOne(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
    private MonikielinenTeksti name;

    /**
     * Translatable koodiValue
     */
    @ManyToOne(cascade= CascadeType.ALL, fetch = FetchType.LAZY)
    private MonikielinenTeksti value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MonikielinenTeksti getName() {
        return name;
    }

    public void setName(MonikielinenTeksti name) {
        this.name = name;
    }

    public MonikielinenTeksti getValue() {
        return value;
    }

    public void setValue(MonikielinenTeksti value) {
        this.value = value;
    }

    /**
     * Using key to index in the hash set.
     */
    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
