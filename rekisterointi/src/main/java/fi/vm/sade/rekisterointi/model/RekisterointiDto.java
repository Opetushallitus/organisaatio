package fi.vm.sade.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class RekisterointiDto {

  @NotNull
  @Valid
  public final Organisaatio organisaatio;

  @NotNull
  public final String tyyppi;

  @NotEmpty
  public final Set<@Email String> sahkopostit;

  @NotNull
  @Valid
  public final Kayttaja kayttaja;

}
