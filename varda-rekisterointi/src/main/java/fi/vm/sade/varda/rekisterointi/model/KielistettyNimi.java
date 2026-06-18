package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of", onConstructor_ = @JsonCreator)
@Table("organisaationimi")
public class KielistettyNimi {

    @NotNull
    public final String nimi;

    @NotNull @Column("nimi_kieli")
    public final String kieli;

    @Column("nimi_alkupvm")
    public final LocalDate alkuPvm;

}
