package fi.vm.sade.rekisterointi.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
@Builder
public class Osoite {

  public static final Osoite TYHJA = Osoite.of("", "", "");

  public final String katuosoite;
  public final String postinumeroUri;
  public final String postitoimipaikka;

}
