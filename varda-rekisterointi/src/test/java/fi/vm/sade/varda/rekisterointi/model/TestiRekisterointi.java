package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

public class TestiRekisterointi {

    public static Rekisterointi validiVardaRekisterointi() {
        return vardaRekisterointi(validiKayttaja());
    }
    public static Rekisterointi validiJotpaRekisterointi() {
        return jotpaRekisterointi(validiKayttaja());
    }

    public static Rekisterointi vardaRekisterointi(Kayttaja kayttaja) {
        return Rekisterointi.of(
                Organisaatio.of("0000001-9", "oid", LocalDate.now(),
                        KielistettyNimi.of("Varda-yritys", "fi", LocalDate.now()),
                        "yritysmuoto_26", Set.of("organisaatiotyyppi_07"), "kunta_091",
                        "maatjavaltiot1_fin", Set.of("oppilaitoksenopetuskieli_1#1"),
                        Yhteystiedot.of("101234567", "testi@testiyritys.fi", Osoite.TYHJA, Osoite.TYHJA),
                        false),
                "varda",
                "vardatoimintamuoto_tm01",
                Collections.singleton("Helsinki"),
                Set.of("foo@foo.bar"),
                kayttaja);
    }

    public static Rekisterointi jotpaRekisterointi(Kayttaja kayttaja) {
        return Rekisterointi.of(
                Organisaatio.of("0000001-9", "oid", LocalDate.now(),
                        KielistettyNimi.of("Jotpa-yritys", "fi", LocalDate.now()),
                        "yritysmuoto_26", Set.of("organisaatiotyyppi_07"), "kunta_091",
                        "maatjavaltiot1_fin", Set.of("oppilaitoksenopetuskieli_1#1"),
                        Yhteystiedot.of("101234567", "testi@testiyritys.fi", Osoite.TYHJA, Osoite.TYHJA),
                        false),
                "jotpa",
                null,
                Collections.emptySet(),
                Set.of("foo@foo.bar"),
                kayttaja);
    }

    private static Kayttaja validiKayttaja() {
        return Kayttaja.builder()
                .etunimi("John")
                .sukunimi("Smith")
                .sahkoposti("john.smith@example.com")
                .asiointikieli("fi")
                .build();
    }

}
