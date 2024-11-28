package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Table("organisaationimi")
public class KielistettyNimi {

    @NotNull
    public final String nimi;

    @NotNull @Column("nimi_kieli")
    public final String kieli;

    @Column("nimi_alkupvm")
    public final LocalDate alkuPvm;

}
