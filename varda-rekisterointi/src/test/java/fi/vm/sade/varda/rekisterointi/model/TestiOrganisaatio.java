package fi.vm.sade.varda.rekisterointi.model;

import java.time.LocalDate;
import java.util.Set;

public class TestiOrganisaatio {

    public static Organisaatio organisaatio(String oid) {
        LocalDate now = LocalDate.now();
        return Organisaatio.of(
                "1234567-8",
                oid,
                now,
                KielistettyNimi.of("Testi", "fi", now),
                "vardatoimintamuoto_tm01",
                Set.of(),
                "kunta_93",
                "maa_123",
                Set.of("fi"),
                Yhteystiedot.of("0123456789", "foo@foo.bar", Osoite.TYHJA, Osoite.TYHJA),
                false
        );
    }

}
