package fi.vm.sade.rekisterointi.rest;

import fi.vm.sade.rekisterointi.client.RekisterointiClient;
import fi.vm.sade.rekisterointi.model.Kayttaja;
import fi.vm.sade.rekisterointi.model.KielistettyNimi;
import fi.vm.sade.rekisterointi.model.Organisaatio;
import fi.vm.sade.rekisterointi.model.Osoite;
import fi.vm.sade.rekisterointi.model.RekisterointiDto;
import fi.vm.sade.rekisterointi.model.RekisterointiRequest;
import fi.vm.sade.rekisterointi.model.Yhteystiedot;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.Set;

import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_BUSINESS_ID;
import static fi.vm.sade.rekisterointi.util.Constants.SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME;
import static fi.vm.sade.rekisterointi.util.ServletUtils.findSessionAttribute;

@RestController
@RequestMapping(RekisterointiController.BASE_PATH)
public class RekisterointiController {
  static final String BASE_PATH = "/hakija/api/rekisterointi";
  private final RekisterointiClient client;

  public RekisterointiController(RekisterointiClient client) {
    this.client = client;
  }

  /**
   * Luo rekisteröintihakemuksen.
   *
   * @param dto            rekisteröintitiedot
   * @param request        HTTP-pyyntö
   * @param authentication tunnistautumistiedot
   *
   * @return osoite, jonne ohjataan rekisteröitymisen jälkeen.
   */
  @PostMapping
  public String register(
      @RequestBody @Validated RekisterointiRequest dto,
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) {
    String ytunnus = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_BUSINESS_ID, String.class)
        .orElseThrow(() -> new RuntimeException("Käyttäjälle ei löydy y-tunnusta istunnosta."));
    String nimi = findSessionAttribute(request, SESSION_ATTRIBUTE_NAME_ORGANISATION_NAME, String.class)
        .orElseThrow(() -> new RuntimeException("Käyttäjälle ei löydy organisaation nimeä istunnosta."));
    var yhteystiedot = Yhteystiedot.of(
        dto.puhelinnumero,
        dto.email,
        Osoite.of(dto.postiosoite, dto.postinumero, dto.postitoimipaikka),
        Osoite.of(dto.kayntiosoite, dto.kayntipostinumero, dto.kayntipostitoimipaikka));
    var organisaatio = Organisaatio.of(
        ytunnus,
        null,
        LocalDate.parse(dto.alkamisaika),
        KielistettyNimi.of(nimi, dto.asiointikieli, null),
        dto.yritysmuoto,
        Set.of("organisaatiotyyppi_01"),
        dto.kotipaikka,
        "maatjavaltiot1_fin",
        Set.of(),
        yhteystiedot,
        false);
    var kayttaja = Kayttaja.of(dto.etunimi, dto.sukunimi, dto.paakayttajaEmail, dto.asiointikieli, dto.info);
    var backendRequest = RekisterointiDto.of(organisaatio, "jotpa", dto.emails, kayttaja);
    client.create(backendRequest);
    return "ok";
  }

}
