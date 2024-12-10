package fi.vm.sade.varda.rekisterointi.model;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class Kasittelyssa {
  @NotNull
  public final String tyyppi;

  @NotNull
  public final Integer amount;
}
