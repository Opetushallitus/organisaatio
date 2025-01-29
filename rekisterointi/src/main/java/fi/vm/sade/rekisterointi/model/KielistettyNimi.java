package fi.vm.sade.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class KielistettyNimi {

  @NotNull
  public final String nimi;

  @NotNull
  public final String kieli;

  public final LocalDate alkuPvm;

}
