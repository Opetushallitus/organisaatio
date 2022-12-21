package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor
public class RekisterointiDto {

    @NotNull
    @Valid
    public final Organisaatio organisaatio;

    public final String toimintamuoto;

    @NotNull
    public final String tyyppi;

    public final Set<@NotNull String> kunnat;

    @NotEmpty
    public final Set<@Email String> sahkopostit;

    @NotNull
    @Valid
    public final Kayttaja kayttaja;

    @AssertTrue(message = "Invalid Varda registration")
    private boolean isValidVardaRekisterointi() {
        return !this.tyyppi.equals("varda")
                || this.kunnat != null && this.kunnat.size() > 0 && this.toimintamuoto != null;
    }
}
