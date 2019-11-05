package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class TestiRekisterointi {

    public static Rekisterointi validiRekisterointi() {
        return rekisterointi(validiKayttaja());
    }

    public static Rekisterointi rekisterointi(Kayttaja kayttaja) {
        return new Rekisterointi(
                null,
                Organisaatio.of("0000000-1", null, LocalDate.now(),
                        KielistettyNimi.of("Testiyritys", "fi", LocalDate.now()),
                        "yritysmuoto_26", Set.of("organisaatiotyyppi_07"), "kunta_091",
                        "maatjavaltiot1_fin", Set.of("oppilaitoksenopetuskieli_1#1"),
                        Yhteystiedot.of("101234567", "testi@testiyritys.fi", Osoite.TYHJA, Osoite.TYHJA)),
                "vardatoimintamuoto_tm01",
                Set.of("kunta_091"),
                Set.of("testi@testiyritys.fi"),
                kayttaja,
                LocalDateTime.now(),
                Rekisterointi.Tila.KASITTELYSSA
        );
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
