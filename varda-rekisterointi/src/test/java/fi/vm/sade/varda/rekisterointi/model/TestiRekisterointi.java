package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

public class TestiRekisterointi {

    public static Rekisterointi validiRekisterointi() {
        return rekisterointi(validiKayttaja());
    }

    public static Rekisterointi rekisterointi(Kayttaja kayttaja) {
        return Rekisterointi.of(
                Organisaatio.of("0000000-1", "oid", LocalDate.now(),
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

    private static Kayttaja validiKayttaja() {
        return Kayttaja.builder()
                .etunimi("John")
                .sukunimi("Smith")
                .sahkoposti("john.smith@example.com")
                .asiointikieli("en")
                .build();
    }

}
