package fi.vm.sade.varda.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class Permissions {
  @NotNull
  public boolean hasCreatePermission;

  @NotNull
  public String[] registrationTypes;
}
