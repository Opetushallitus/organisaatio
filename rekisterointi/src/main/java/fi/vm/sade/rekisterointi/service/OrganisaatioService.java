package fi.vm.sade.rekisterointi.service;

import fi.vm.sade.rekisterointi.client.KoodistoClient;
import fi.vm.sade.rekisterointi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Palvelu organisaatiotietojen muunnoksille.
 */
@Service
public class OrganisaatioService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganisaatioService.class);

  private static final String DEFAULT_KIELI_KOODI_URI_VERSION = "kieli_fi#1";
  private static final String DEFAULT_KIELI_KOODI_ARVO = "fi";
  private static final Map<String, String> KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO = Map.of(
      "kieli_fi#1", "fi",
      "kieli_sv#1", "sv",
      "kieli_en#1", "en");
  private static final Set<String> DEFAULT_OPETUSKIELET = Set.of("oppilaitoksenopetuskieli_1#1");
  private static final String PUHELIN_TYYPPI = "puhelin";
  private static final String EMAIL_TYYPPI = "email";
  private static final Pattern YRITYSMUOTOURI_PATTERN = Pattern.compile("yritysmuoto_\\d+");

  private final Map<String, Koodi> yritysmuotoUriToKoodi = new HashMap<>();

  public OrganisaatioService(KoodistoClient koodistoClient) {
    koodistoClient.listKoodit(KoodistoType.YRITYSMUOTO).forEach(
        koodi -> yritysmuotoUriToKoodi.put(koodi.uri, koodi));
  }

  /**
   * Muuntaa organisaatiopalvelun DTO:sta organisaatio-olioksi.
   *
   * @param dto DTO
   * @return organisaatio-olio.
   */
  public Organisaatio muunnaV4Dto(OrganisaatioV4Dto dto) {
    return Organisaatio.of(
        dto.ytunnus,
        dto.oid,
        dto.alkuPvm,
        kuranttiNimi(dto),
        dto.yritysmuoto,
        dto.tyypit,
        dto.kotipaikkaUri,
        dto.maaUri,
        dto.kieletUris,
        muunnaYhteystiedot(dto),
        false);
  }

  /**
   * Muuntaa organisaatio-oliosta DTO:ksi.
   *
   * @param organisaatio organisaatio-olio
   * @return organisaatiopalvelun DTO.
   */
  public OrganisaatioV4Dto muunnaOrganisaatio(Organisaatio organisaatio) {
    OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
    dto.ytunnus = organisaatio.ytunnus;
    dto.alkuPvm = organisaatio.alkuPvm;
    dto.nimet = organisaatioNimet(organisaatio.ytjNimi);
    dto.nimi = dto.nimet.get(0).nimi;
    dto.ytjkieli = koodiArvoToKieliKoodiUriVersion(organisaatio.ytjNimi.kieli);
    dto.yritysmuoto = yritysMuotoKoodiUriToNimi(organisaatio.yritysmuoto);
    dto.tyypit = organisaatio.tyypit;
    dto.kotipaikkaUri = organisaatio.kotipaikkaUri;
    dto.maaUri = organisaatio.maaUri;
    dto.kieletUris = opetusKielet(organisaatio.kieletUris);
    dto.yhteystiedot = muunnaYhteystiedot(organisaatio);
    return dto;
  }

  KielistettyNimi kuranttiNimi(OrganisaatioV4Dto dto) {
    LocalDate now = LocalDate.now();
    OrganisaatioNimi kurantti = dto.nimet.stream()
        .reduce((nimi1, nimi2) -> {
          LocalDate alkuPvm1 = nullSafeDate(nimi1.alkuPvm);
          LocalDate alkuPvm2 = nullSafeDate(nimi2.alkuPvm);
          if (-DAYS.between(alkuPvm2, now) < DAYS.between(alkuPvm1, now)) {
            return nimi2;
          }
          return nimi1;
        })
        .orElseThrow(() -> new IllegalStateException("Ei voimassa olevaa nimeä organisaatiolle: " + dto.ytunnus));
    String ytjKieli = ytjKieliTaiOletusKieli(dto);
    String kieli = kieliKoodiUriVersionToKoodiArvo(ytjKieli);
    String ytjKielinen = kurantti.nimi.getOrDefault(kieli,
        kurantti.nimi.getOrDefault("sv", kurantti.nimi.get(DEFAULT_KIELI_KOODI_ARVO)));
    if (ytjKielinen == null) {
      LOGGER.warn("Ei YTJ-kielen tai oletuskielen mukaista nimeä organisaatiolle: {}", dto.ytunnus);
      ytjKielinen = "";
    }
    return KielistettyNimi.of(ytjKielinen, kieli, kurantti.alkuPvm);
  }

  List<OrganisaatioNimi> organisaatioNimet(KielistettyNimi kielistettyNimi) {
    OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
    organisaatioNimi.nimi = Map.of(kielistettyNimi.kieli, kielistettyNimi.nimi);
    organisaatioNimi.alkuPvm = kielistettyNimi.alkuPvm != null ? kielistettyNimi.alkuPvm : LocalDate.now();
    return List.of(organisaatioNimi);
  }

  // organisaatiopalvelu tallentaa ikävä kyllä kielistettyjä yritysmuotoja,
  // muunnetaan kunnes saadaan organisaatiopalvelu järkevämpään kuosiin
  String yritysMuotoKoodiUriToNimi(String uri) {
    String nimi = null;
    if (YRITYSMUOTOURI_PATTERN.matcher(uri).matches()) {
      LOGGER.debug("Muunnetaan yritysmuotokoodi: {}", uri);
      Koodi koodi = yritysmuotoUriToKoodi.get(uri);
      if (koodi != null) {
        nimi = koodi.nimi.get(DEFAULT_KIELI_KOODI_ARVO);
      } else {
        LOGGER.warn("Ei nimeä yritysmuotokoodille: {}", uri);
      }
    }
    return nimi != null ? nimi : uri;
  }

  private static String ytjKieliTaiOletusKieli(OrganisaatioV4Dto dto) {
    return dto.ytjkieli != null ? dto.ytjkieli : DEFAULT_KIELI_KOODI_URI_VERSION;
  }

  private LocalDate nullSafeDate(LocalDate date) {
    if (date == null) {
      return LocalDate.MIN;
    }
    return date;
  }

  private static String kieliKoodiUriVersionToKoodiArvo(String koodiUriVersion) {
    return KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO.getOrDefault(koodiUriVersion, "fi");
  }

  private static String koodiArvoToKieliKoodiUriVersion(String kieli) {
    return KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO.entrySet().stream()
        .filter(entry -> entry.getValue().equals(kieli)).findAny()
        .orElseThrow(
            () -> new IllegalStateException("Ei sallittu kieli: " + kieli))
        .getKey();
  }

  private static Yhteystiedot muunnaYhteystiedot(OrganisaatioV4Dto dto) {
    String ytjKieli = ytjKieliTaiOletusKieli(dto);
    List<YhteystietoDto> yhteystiedot = dto.yhteystiedot != null ? dto.yhteystiedot : List.of();
    Map<String, List<YhteystietoDto>> yhteystiedotTyypeittain = yhteystiedot.stream()
        .filter(yhteystietoDto -> yhteystietoDto.kieli.equals(ytjKieli))
        .collect(Collectors.groupingBy(yhteystietoDto -> yhteystietoDto.osoiteTyyppi != null
            ? yhteystietoDto.osoiteTyyppi
            : yhteystietoDto.numero != null ? PUHELIN_TYYPPI : EMAIL_TYYPPI));
    return Yhteystiedot.of(
        poimiPuhelin(yhteystiedotTyypeittain),
        poimiEmail(yhteystiedotTyypeittain),
        poimiOsoite(yhteystiedotTyypeittain, OsoiteTyyppi.POSTI),
        poimiOsoite(yhteystiedotTyypeittain, OsoiteTyyppi.KAYNTI));
  }

  private static String poimiPuhelin(Map<String, List<YhteystietoDto>> tiedot) {
    return nullToBlank(tiedot.getOrDefault(PUHELIN_TYYPPI, List.of()).stream()
        .filter(dto -> dto.numero != null).findAny().orElse(new YhteystietoDto()).numero);
  }

  private static String poimiEmail(Map<String, List<YhteystietoDto>> tiedot) {
    return nullToBlank(tiedot.getOrDefault(EMAIL_TYYPPI, List.of()).stream()
        .filter(dto -> dto.email != null).findAny().orElse(new YhteystietoDto()).email);
  }

  private static Osoite poimiOsoite(Map<String, List<YhteystietoDto>> tiedot, OsoiteTyyppi tyyppi) {
    return tiedot.getOrDefault(tyyppi.value(), List.of()).stream()
        .findAny()
        .map(yhteystietoDto -> Osoite.builder()
            .katuosoite(yhteystietoDto.osoite)
            .postinumeroUri(yhteystietoDto.postinumeroUri)
            .postitoimipaikka(yhteystietoDto.postitoimipaikka)
            .build())
        .orElse(Osoite.TYHJA);
  }

  private static Set<String> opetusKielet(Set<String> kieletUris) {
    if (kieletUris == null || kieletUris.isEmpty()) {
      return DEFAULT_OPETUSKIELET;
    }
    return kieletUris;
  }

  private static List<YhteystietoDto> muunnaYhteystiedot(Organisaatio organisaatio) {
    String ytjKieli = koodiArvoToKieliKoodiUriVersion(organisaatio.ytjNimi.kieli);
    YhteystietoDto email = new YhteystietoDto();
    email.kieli = ytjKieli;
    email.email = organisaatio.yhteystiedot.sahkoposti;
    YhteystietoDto puhelin = new YhteystietoDto();
    puhelin.kieli = ytjKieli;
    puhelin.numero = organisaatio.yhteystiedot.puhelinnumero;
    puhelin.tyyppi = "puhelin";
    YhteystietoDto postiosoite = muunnaOsoite(ytjKieli, OsoiteTyyppi.POSTI, organisaatio.yhteystiedot.postiosoite);
    YhteystietoDto kayntiosoite = muunnaOsoite(ytjKieli, OsoiteTyyppi.KAYNTI, organisaatio.yhteystiedot.kayntiosoite);
    return List.of(email, puhelin, postiosoite, kayntiosoite);
  }

  private static YhteystietoDto muunnaOsoite(String kieli, OsoiteTyyppi tyyppi, Osoite osoite) {
    YhteystietoDto dto = new YhteystietoDto();
    dto.osoiteTyyppi = tyyppi.value();
    dto.kieli = kieli;
    dto.osoite = osoite.katuosoite;
    dto.postinumeroUri = osoite.postinumeroUri;
    dto.postitoimipaikka = osoite.postitoimipaikka;
    return dto;
  }

  private static String nullToBlank(String value) {
    return value == null ? "" : value;
  }

}
