package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor
public class Rekisterointi {

    @With
    @Id
    public final Long id;

    @With
    @NotNull
    @Column("rekisterointi_id")
    public final Organisaatio organisaatio;

    @NotNull
    public final String tyyppi;

    @NotNull
    public final String toimintamuoto;

    @NotEmpty
    public final Set<@NotNull String> kunnat;

    @NotEmpty
    public final Set<@Email String> sahkopostit;

    @With
    @NotNull
    public final Kayttaja kayttaja;

    @NotNull
    public final LocalDateTime vastaanotettu;

    @Column("rekisterointi_id")
    public final Paatos paatos;

    @NotNull
    public final Tila tila;

    public static Rekisterointi of(
            Organisaatio organisaatio,
            String tyyppi,
            String toimintamuoto,
            Set<String> kunnat,
            Set<String> sahkopostit,
            Kayttaja kayttaja) {
        return new Rekisterointi(null, organisaatio, tyyppi, toimintamuoto, kunnat, sahkopostit, kayttaja,
                LocalDateTime.now(), null, Tila.KASITTELYSSA);
    }

    public static Rekisterointi from(RekisterointiDto dto) {
        return Rekisterointi.of(
                dto.organisaatio,
                dto.tyyppi,
                dto.toimintamuoto,
                dto.kunnat,
                dto.sahkopostit,
                dto.kayttaja);
    }

    public Rekisterointi withPaatos(Paatos paatos) {
        return new Rekisterointi(
                this.id,
                this.organisaatio,
                this.tyyppi,
                this.toimintamuoto,
                this.kunnat,
                this.sahkopostit,
                this.kayttaja,
                this.vastaanotettu,
                paatos,
                paatos.hyvaksytty ? Tila.HYVAKSYTTY : Tila.HYLATTY);
    }

    public enum Tila {
        KASITTELYSSA,
        HYVAKSYTTY,
        HYLATTY
    }

}
