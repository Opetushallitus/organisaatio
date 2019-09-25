package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

public class Rekisterointi {

    @Id
    public final Long id;

    @NotNull
    public final ObjectNode organisaatio;
    @NotEmpty
    public final Set<@NotNull String> kunnat;
    public final Set<@Email String> sahkopostit;
    @NotNull
    public final String toimintamuoto;

    @NotNull @Valid
    public final Kayttaja kayttaja;

    @NotNull
    public final LocalDateTime vastaanotettu;

    public final Paatos paatos;

    private Rekisterointi(
            Long id,
            ObjectNode organisaatio,
            Set<String> kunnat,
            Set<String> sahkopostit,
            String toimintamuoto,
            Kayttaja kayttaja,
            LocalDateTime vastaanotettu,
            Paatos paatos) {
        this.id = id;
        this.organisaatio = organisaatio;
        this.kunnat = kunnat;
        this.sahkopostit = sahkopostit;
        this.toimintamuoto = toimintamuoto;
        this.kayttaja = kayttaja;
        this.vastaanotettu = vastaanotettu;
        this.paatos = paatos;
    }

    public static Rekisterointi of(
            ObjectNode organisaatio,
            Set<String> kunnat,
            Set<String> sahkopostit,
            String toimintamuoto,
            Kayttaja kayttaja) {
        return new Rekisterointi(
                null, organisaatio, kunnat, sahkopostit, toimintamuoto, kayttaja, LocalDateTime.now(), null);
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
                this.paatos
        );
    }

    public Rekisterointi withPaatos(Paatos paatos) {
        return new Rekisterointi(
                this.id,
                this.organisaatio,
                this.kunnat,
                this.sahkopostit,
                this.toimintamuoto,
                this.kayttaja,
                this.vastaanotettu,
                paatos
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Rekisterointi) {
            Rekisterointi toinen = (Rekisterointi) other;
            return new EqualsBuilder()
                    .append(id, toinen.id)
                    .append(organisaatio, toinen.organisaatio)
                    .append(sahkopostit, toinen.sahkopostit)
                    .append(toimintamuoto, toinen.toimintamuoto)
                    .append(kayttaja, toinen.kayttaja)
                    .append(vastaanotettu, toinen.vastaanotettu)
                    .append(paatos, toinen.paatos)
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(organisaatio)
                .append(sahkopostit)
                .append(toimintamuoto)
                .append(kayttaja)
                .append(vastaanotettu)
                .append(paatos)
                .hashCode();
    }

}
