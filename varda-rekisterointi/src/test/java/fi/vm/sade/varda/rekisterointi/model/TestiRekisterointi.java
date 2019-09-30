package fi.vm.sade.varda.rekisterointi.model;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;

public class TestiRekisterointi {

    public static Rekisterointi validiRekisterointi() {
        return rekisterointi(validiKayttaja());
    }

    public static Rekisterointi rekisterointi(Kayttaja kayttaja) {
        return Rekisterointi.of(
                new ObjectNode(JsonNodeFactory.instance),
                Collections.singleton("Helsinki"),
                Collections.emptySet(),
                "vardatoimintamuoto_tm01",
                kayttaja
        );
    }

    private static Kayttaja validiKayttaja() {
        Kayttaja kayttaja = new Kayttaja();
        kayttaja.etunimi = "John";
        kayttaja.sukunimi = "Smith";
        kayttaja.sahkoposti = "john.smith@example.com";
        kayttaja.asiointikieli = "en";
        return kayttaja;
    }

}
