package fi.vm.sade.varda.rekisterointi.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

public class Paatos {

    @NotNull @Column("rekisterointi_id")
    public final Long rekisterointi;
    @NotNull
    public final boolean hyvaksytty;
    @NotNull
    public final Timestamp paatetty;
    @NotNull @Column("paattaja_id")
    public final Long paattaja;
    public final String perustelu;

    private Paatos(Long rekisterointi, boolean hyvaksytty, Timestamp paatetty, Long paattaja, String perustelu) {
        this.rekisterointi = rekisterointi;
        this.hyvaksytty = hyvaksytty;
        this.paatetty = paatetty;
        this.paattaja = paattaja;
        this.perustelu = perustelu;
    }

    public static Paatos of(Long rekisterointi, boolean hyvaksytty, Long paattaja, String perustelu) {
        return new Paatos(rekisterointi, hyvaksytty, new Timestamp(System.currentTimeMillis()), paattaja, perustelu);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Paatos) {
            Paatos toinen = (Paatos) other;
            return new EqualsBuilder()
                    .append(rekisterointi, toinen.rekisterointi)
                    .append(hyvaksytty, toinen.hyvaksytty)
                    .append(paatetty, toinen.paatetty)
                    .append(paattaja, toinen.paattaja)
                    .append(perustelu, toinen.perustelu)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(rekisterointi)
                .append(hyvaksytty)
                .append(paatetty)
                .append(paattaja)
                .append(perustelu)
                .hashCode();
    }
}
