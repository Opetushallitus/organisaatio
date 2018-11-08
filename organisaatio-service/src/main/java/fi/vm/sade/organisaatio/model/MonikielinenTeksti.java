package fi.vm.sade.organisaatio.model;

import com.google.common.base.Objects;
import fi.vm.sade.security.xssfilter.XssFilter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic translatable text.
 *
 * @author jraanamo
 * @author mlyly
 */
@Entity
public class MonikielinenTeksti extends BaseEntity {

	private static final long serialVersionUID = 1L;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="key")
    @Column(name="value", length=16384)
    @CollectionTable(joinColumns=@JoinColumn(name="id"))
    @BatchSize(size = 1000)
    private Map<String, String> values = new HashMap<>();


    @PrePersist
    @PreUpdate
    public void filterXss() {
    	for (Map.Entry<String, String> e : values.entrySet()) {
    		e.setValue(XssFilter.filter(e.getValue()));
            // Allow ampersand characters
            e.setValue(e.getValue().replace("&amp;", "&"));
    	}
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public void addString(String key, String value) {
        if (value == null) {
            getValues().remove(key);
        } else {
            getValues().put(key, value);
        }
    }

    public String getString(String key) {
        return getValues().get(key);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof MonikielinenTeksti) {
            return Objects.equal(this.values, ((MonikielinenTeksti)o).values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.values);
    }
}
