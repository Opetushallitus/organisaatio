package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.util.Set;

public class TestiOrganisaatio {

    public static Organisaatio organisaatio(String oid) {
        return organisaatio(oid, Set.of());
    }

    public static Organisaatio jotpaOrganisaatio(String oid) {
        return organisaatio(oid, Set.of("organisaatiotyyppi_01"));
    }

    private static Organisaatio organisaatio(String oid, Set<String> organisaatiotyypit) {
        LocalDate now = LocalDate.now();
        return Organisaatio.of(
                "1234567-8",
                oid,
                now,
                KielistettyNimi.of("Testi", "fi", now),
                "vardatoimintamuoto_tm01",
                organisaatiotyypit,
                "kunta_93",
                "maa_123",
                Set.of("fi"),
                Yhteystiedot.of("0123456789", "foo@foo.bar", Osoite.TYHJA, Osoite.TYHJA),
                false
        );
    }

}
