package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.Collections;

public class TestiRekisterointi {

    public static Rekisterointi validiRekisterointi() {
        return rekisterointi(validiKayttaja());
    }

    public static Rekisterointi rekisterointi(Kayttaja kayttaja) {
        return new Rekisterointi(
                null,
                new ObjectNode(JsonNodeFactory.instance),
                Collections.singleton("Helsinki"),
                Collections.emptySet(),
                "vardatoimintamuoto_tm01",
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
