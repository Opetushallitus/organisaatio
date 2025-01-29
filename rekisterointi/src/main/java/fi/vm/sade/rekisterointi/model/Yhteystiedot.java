package fi.vm.sade.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.With;

import jakarta.validation.constraints.NotNull;

@AllArgsConstructor(staticName = "of")
public class Yhteystiedot {

  @NotNull
  public final String puhelinnumero;
  @NotNull
  public final String sahkoposti;
  @With
  public final Osoite postiosoite;
  @With
  public final Osoite kayntiosoite;

}
