package fi.vm.sade.rekisterointi.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

@EqualsAndHashCode
@AllArgsConstructor
public class RekisterointiRequest {

  @NotNull
  public final String yritysmuoto;
  @NotNull
  public final String kotipaikka;
  @NotNull
  public final String alkamisaika;
  @NotNull
  public final String puhelinnumero;
  @NotNull
  public final String email;
  @NotNull
  public final String postiosoite;
  @NotNull
  public final String postinumero;
  @NotNull
  public final String postitoimipaikka;
  @NotNull
  public final Boolean copyKayntiosoite;
  @NotNull
  public final String kayntiosoite;
  @NotNull
  public final String kayntipostinumero;
  @NotNull
  public final String kayntipostitoimipaikka;
  @NotNull
  public final Set<String> emails;
  @NotNull
  public final String etunimi;
  @NotNull
  public final String sukunimi;
  @NotNull
  public final String paakayttajaEmail;
  @NotNull
  public final String asiointikieli;
  public final String info;
}
