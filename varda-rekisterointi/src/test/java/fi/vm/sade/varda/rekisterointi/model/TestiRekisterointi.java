package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

public class TestiRekisterointi {

    public static Rekisterointi validiRekisterointi() {
        return rekisterointi(validiKayttaja());
    }

    public static Rekisterointi rekisterointi(Kayttaja kayttaja) {
        return new Rekisterointi(
                null,
                Organisaatio.of("0000000-1", null, LocalDate.now(),
                        KielistettyNimi.of("Testiyritys", "fi", LocalDate.now()),
                        "oy_foo_ab", Collections.singleton("tyyppi"), "Helsinki",
                        "Suomi", Collections.emptySet()),
                "vardatoimintamuoto_tm01",
                Collections.singleton("Helsinki"),
                Collections.emptySet(),
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
