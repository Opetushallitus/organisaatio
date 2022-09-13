package fi.vm.sade.rekisterointi.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
public class Kayttaja {
  @NotNull
  public final String etunimi;
  @NotNull
  public final String sukunimi;
  @NotNull
  @Email
  public final String sahkoposti;
  @NotNull
  public final String asiointikieli;
  public final String saateteksti;

}
