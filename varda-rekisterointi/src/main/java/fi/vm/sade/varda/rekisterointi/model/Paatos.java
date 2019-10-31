package fi.vm.sade.varda.rekisterointi.model;

import lombok.Value;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
public class Paatos {

    @NotNull @Column("rekisterointi_id")
    public final Long rekisterointi;
    @NotNull
    public final boolean hyvaksytty;
    @NotNull
    public final LocalDateTime paatetty;
    @NotNull @Column("paattaja_oid")
    public final String paattaja;
    public final String perustelu;

}
