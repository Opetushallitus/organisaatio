package fi.vm.sade.varda.rekisterointi.model;

import lombok.EqualsAndHashCode;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode
public class Rekisterointi {

    @With @Id
    public final Long id;

    @With @NotNull @Column("rekisterointi_id")
    public final Organisaatio organisaatio;

    @NotNull
    public final String toimintamuoto;

    @NotEmpty
    public final Set<@NotNull String> kunnat;
    public final Set<@Email String> sahkopostit;

    @With @NotNull @Valid
    public final Kayttaja kayttaja;

    @With
    public final LocalDateTime vastaanotettu;

    @With
    public final Tila tila;

    public Rekisterointi(
            Long id,
            Organisaatio organisaatio,
            String toimintamuoto,
            Set<String> kunnat,
            Set<String> sahkopostit,
            Kayttaja kayttaja,
            LocalDateTime vastaanotettu,
            Tila tila) {
        this.id = id;
        this.organisaatio = organisaatio;
        this.toimintamuoto = toimintamuoto;
        this.kunnat = kunnat;
        this.sahkopostit = sahkopostit;
        this.kayttaja = kayttaja;
        this.vastaanotettu = vastaanotettu != null ? vastaanotettu : LocalDateTime.now();
        this.tila = tila != null ? tila : Tila.KASITTELYSSA;
    }

    public static Rekisterointi of(
            Organisaatio organisaatio,
            String toimintamuoto,
            Set<String> kunnat,
            Set<String> sahkopostit,
            Kayttaja kayttaja) {
        return new Rekisterointi(
                null, organisaatio, toimintamuoto, kunnat, sahkopostit, kayttaja, null, null);
    }

    public enum Tila {
        KASITTELYSSA,
        HYVAKSYTTY,
        HYLATTY
    }

}
