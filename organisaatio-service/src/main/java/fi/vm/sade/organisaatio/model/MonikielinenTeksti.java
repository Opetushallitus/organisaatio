package fi.vm.sade.organisaatio.model;

import com.google.common.base.Objects;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@BatchSize(size = 500)
@Table(name="monikielinenteksti")
public class MonikielinenTeksti extends BaseEntity {

	private static final long serialVersionUID = 1L;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name="key")
    @Column(name="value", length=16384)
    @CollectionTable(name="monikielinenteksti_values", joinColumns=@JoinColumn(name="id"))
    @BatchSize(size = 1000)
    private Map<String, String> values = new HashMap<>();

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
    public int hashCode() {
        return Objects.hashCode(this.values);
    }
}
