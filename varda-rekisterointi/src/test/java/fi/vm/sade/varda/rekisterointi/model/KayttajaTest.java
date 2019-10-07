package fi.vm.sade.varda.rekisterointi.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;

public class KayttajaTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void builderThrowsOnMissingEtunimi() {
        testMissingField("etunimi",
            Kayttaja.builder()
                    .sukunimi("Henkilö")
                    .sahkoposti("testi.henkilo@foo.bar")
                    .asiointikieli("fi"));
    }

    @Test
    public void builderThrowsOnMissingSukunimi() {
        testMissingField("sukunimi",
                Kayttaja.builder()
                        .etunimi("Testi")
                        .sahkoposti("testi.henkilo@foo.bar")
                        .asiointikieli("fi"));
    }

    @Test
    public void builderThrowsOnMissingSahkoposti() {
        testMissingField("sähköposti",
                Kayttaja.builder()
                        .etunimi("Testi")
                        .sukunimi("Henkilö")
                        .asiointikieli("fi"));
    }

    @Test
    public void builderThrowsOnMissingAsiointiKieli() {
        testMissingField("asiointikieli",
                Kayttaja.builder()
                        .etunimi("Testi")
                        .sukunimi("Henkilö")
                        .sahkoposti("testi.henkilo@foo.bar"));
    }

    @Test
    public void builderAcceptsMissingSaateteksti() {
        Kayttaja kayttaja = Kayttaja.builder()
                .etunimi("Testi")
                .sukunimi("Henkilö")
                .sahkoposti("testi.henkilo@foo.bar")
                .asiointikieli("fi")
                .build();
        assertNotNull(kayttaja);
    }

    private void testMissingField(String field, Kayttaja.Builder builder) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(field);
        builder.build();
    }

}
