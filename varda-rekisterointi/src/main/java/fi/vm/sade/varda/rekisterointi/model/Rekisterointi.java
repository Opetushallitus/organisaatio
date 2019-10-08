package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.With;
import org.springframework.data.annotation.Id;

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

    @NotNull
    public final ObjectNode organisaatio;
    @NotEmpty
    public final Set<@NotNull String> kunnat;
    public final Set<@Email String> sahkopostit;
    @NotNull
    public final String toimintamuoto;

    @With @NotNull @Valid
    public final Kayttaja kayttaja;

    @With
    public final LocalDateTime vastaanotettu;

    @With
    public final Tila tila;

    public Rekisterointi(
            Long id,
            ObjectNode organisaatio,
            Set<String> kunnat,
            Set<String> sahkopostit,
            String toimintamuoto,
            Kayttaja kayttaja,
            LocalDateTime vastaanotettu,
            Tila tila) {
        this.id = id;
        this.organisaatio = organisaatio;
        this.kunnat = kunnat;
        this.sahkopostit = sahkopostit;
        this.toimintamuoto = toimintamuoto;
        this.kayttaja = kayttaja;
        this.vastaanotettu = vastaanotettu != null ? vastaanotettu : LocalDateTime.now();
        this.tila = tila != null ? tila : Tila.KASITTELYSSA;
    }

    public static Rekisterointi of(
            ObjectNode organisaatio,
            Set<String> kunnat,
            Set<String> sahkopostit,
            String toimintamuoto,
            Kayttaja kayttaja) {
        return new Rekisterointi(
                null, organisaatio, kunnat, sahkopostit, toimintamuoto, kayttaja, null, null);
    }

    public Rekisterointi withId(Long id) {
        return new Rekisterointi(
                id,
                this.organisaatio,
                this.kunnat,
                this.sahkopostit,
                this.toimintamuoto,
                this.kayttaja,
                this.vastaanotettu,
                this.tila
        );
    }

    public Rekisterointi withTila(Tila tila) {
        return new Rekisterointi(
                this.id,
                this.organisaatio,
                this.kunnat,
                this.sahkopostit,
                this.toimintamuoto,
                this.kayttaja,
                this.vastaanotettu,
                tila
        );
    }

    public enum Tila {
        KASITTELYSSA,
        HYVAKSYTTY,
        HYLATTY
    }

}
