package fi.vm.sade.varda.rekisterointi.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RekisterointiTest {

    @Test
    public void rekisterointiIlmanPaatostaOnKasittelyssa() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        assertEquals(Rekisterointi.Tila.KASITTELYSSA, rekisterointi.tila);
    }

    @Test
    public void rekisterointiOnHylatty() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        rekisterointi = rekisterointi.withPaatos(Paatos.of(rekisterointi.id, false, 1L, "Ei hyvä!"));
        assertEquals(Rekisterointi.Tila.HYLATTY, rekisterointi.tila);
    }

    @Test
    public void rekisterointiOnHyvaksytty() {
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        rekisterointi = rekisterointi.withPaatos(Paatos.of(rekisterointi.id, true, 1L, "Ei paha!"));
        assertEquals(Rekisterointi.Tila.HYVAKSYTTY, rekisterointi.tila);
    }

}
