package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class OrganisaatioService {

    private static final String DEFAULT_KIELI_KOODI_URI_VERSION = "kieli_fi#1";
    private static final String DEFAULT_KIELI_KOODI_ARVO = "fi";
    private static final Map<String, String> KIELI_KOODI_URI_VERSION_TO_KOODI_ARVO = Map.of(
            "kieli_fi#1", "fi",
            "kieli_sv#1", "sv",
            "kieli_en#1", "en"
    );
    private static final List<String> HALUTUT_OSOITETYYPIT = List.of(
            OsoiteTyyppi.POSTI.value(),
            OsoiteTyyppi.KAYNTI.value()
    );
    private static final String PUHELIN_TYYPPI = "puhelin";
    private static final String EMAIL_TYYPPI = "email";

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
                muunnaYhteystiedot(dto));
    }

    public OrganisaatioV4Dto muunnaOrganisaatio(Organisaatio organisaatio) {
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.ytunnus = organisaatio.ytunnus;
        dto.alkuPvm = organisaatio.alkuPvm;
        dto.nimet = organisaatioNimet(organisaatio.ytjNimi);
        dto.nimi = dto.nimet.get(0).nimi;
        dto.ytjkieli = organisaatio.ytjNimi.kieli;
        dto.yritysmuoto = organisaatio.yritysmuoto;
        dto.tyypit = organisaatio.tyypit;
        dto.kotipaikkaUri = organisaatio.kotipaikkaUri;
        dto.maaUri = organisaatio.maaUri;
        dto.kieletUris = organisaatio.kieletUris;
        dto.yhteystiedot = muunnaYhteystiedot(organisaatio);
        return dto;
    }

    KielistettyNimi kuranttiNimi(OrganisaatioV4Dto dto) {
        LocalDate now = LocalDate.now();
        OrganisaatioNimi kurantti = dto.nimet.stream()
                .reduce((nimi1, nimi2) -> {
                    LocalDate alkuPvm1 = nullSafeDate(nimi1.alkuPvm);
                    LocalDate alkuPvm2 = nullSafeDate(nimi2.alkuPvm);
                    if (0 - DAYS.between(alkuPvm2, now) < DAYS.between(alkuPvm1, now)) {
                        return nimi2;
                    }
                    return nimi1;
                })
                .orElseThrow(() -> new IllegalStateException("Ei voimassa olevaa nimeä organisaatiolle: " + dto.ytunnus));
        String ytjKieli = ytjKieliTaiOletusKieli(dto);
        String kieli = kieliKoodiUriVersionToKoodiArvo(ytjKieli);
        String ytjKielinen = kurantti.nimi.getOrDefault(kieli, kurantti.nimi.get(DEFAULT_KIELI_KOODI_ARVO));
        if (ytjKielinen == null) {
            throw new IllegalStateException("Ei YTJ-kielen tai oletuskielen mukaista nimeä organisaatiolle: " + dto.ytunnus);
        }
        return KielistettyNimi.of(ytjKielinen, kieli, kurantti.alkuPvm);
    }

    List<OrganisaatioNimi> organisaatioNimet(KielistettyNimi kielistettyNimi) {
        OrganisaatioNimi organisaatioNimi = new OrganisaatioNimi();
        organisaatioNimi.nimi = Map.of(kielistettyNimi.kieli, kielistettyNimi.nimi);
        organisaatioNimi.alkuPvm = kielistettyNimi.alkuPvm != null ? kielistettyNimi.alkuPvm : LocalDate.now();
        return List.of(organisaatioNimi);
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
                    () -> new IllegalStateException("Ei sallittu kieli: " + kieli)
                ).getKey();
    }

    private static Yhteystiedot muunnaYhteystiedot(OrganisaatioV4Dto dto) {
        String ytjKieli = ytjKieliTaiOletusKieli(dto);
        Map<String, List<YhteystietoDto>> yhteystiedotTyypeittain = dto.yhteystiedot.stream()
                .filter(yhteystietoDto -> yhteystietoDto.kieli.equals(ytjKieli)
                        && HALUTUT_OSOITETYYPIT.contains(yhteystietoDto.osoiteTyyppi))
                .collect(Collectors.groupingBy(yhteystietoDto -> yhteystietoDto.osoiteTyyppi != null
                        ? yhteystietoDto.osoiteTyyppi
                        : yhteystietoDto.numero != null ? PUHELIN_TYYPPI : EMAIL_TYYPPI
                ));
        return Yhteystiedot.of(
                poimiPuhelin(yhteystiedotTyypeittain),
                poimiEmail(yhteystiedotTyypeittain),
                poimiOsoite(yhteystiedotTyypeittain, OsoiteTyyppi.POSTI),
                poimiOsoite(yhteystiedotTyypeittain, OsoiteTyyppi.KAYNTI));
    }

    private static String poimiPuhelin(Map<String, List<YhteystietoDto>> tiedot) {
        return tiedot.getOrDefault(PUHELIN_TYYPPI, List.of()).stream()
                .filter(dto -> dto.numero != null).findAny().orElse(new YhteystietoDto()).numero;
    }

    private static String poimiEmail(Map<String, List<YhteystietoDto>> tiedot) {
        return tiedot.getOrDefault(EMAIL_TYYPPI, List.of()).stream()
                .filter(dto -> dto.email != null).findAny().orElse(new YhteystietoDto()).email;
    }

    private static Osoite poimiOsoite(Map<String, List<YhteystietoDto>> tiedot, OsoiteTyyppi tyyppi) {
        return tiedot.getOrDefault(tyyppi.value(), List.of()).stream()
                .findAny()
                .map(yhteystietoDto -> Osoite.builder()
                            .katuosoite(yhteystietoDto.osoite)
                            .postinumeroUri(yhteystietoDto.postinumeroUri)
                            .postitoimipaikka(yhteystietoDto.postitoimipaikka)
                            .build()
                ).orElse(Osoite.TYHJA);
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

}
