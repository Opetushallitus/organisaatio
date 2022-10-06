package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import org.junit.Test;

import java.time.LocalDateTime;

import static fi.vm.sade.varda.rekisterointi.service.OrganisaatioKayttajaFinalizer.*;
import static org.mockito.Mockito.*;

public class OrganisaatioKayttajaFinalizerTest {

    private static final Long PK_RYHMA_ID = 1L;
    private static final Long PPH_RYHMA_ID = 2L;
    private static final Long RPPH_RYHMA_ID = 3L;
    private static final Long JOTPA_RYHMA_ID = 4L;

    @Test
    public void kutsuuToimintamuodonMukaiseenRyhmaan() {
        KayttooikeusClient kayttooikeusClient = mock(KayttooikeusClient.class);
        OphProperties properties = mock(OphProperties.class);
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_PAIVAKOTI_PROPERTY)).thenReturn(PK_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_PERHEPAIVAHOITAJA_PROPERTY)).thenReturn(PPH_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_RYHMAPERHEPAIVAKOTI_PROPERTY)).thenReturn(RPPH_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_JOTPA_PROPERTY)).thenReturn(JOTPA_RYHMA_ID.toString());
        OrganisaatioKayttajaFinalizer finalizer = new OrganisaatioKayttajaFinalizer(kayttooikeusClient, properties);

        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi().withPaatos(
                new Paatos(true, LocalDateTime.now(), "123", null));
        finalizer.kutsuKayttaja(rekisterointi);
        verify(kayttooikeusClient).kutsuKayttaja(
                rekisterointi.paatos.paattaja,
                rekisterointi.kayttaja,
                rekisterointi.organisaatio.oid,
                PK_RYHMA_ID,
                "VARDA"
        );
    }

    @Test
    public void kutsuuJotpaRyhmaan() {
        KayttooikeusClient kayttooikeusClient = mock(KayttooikeusClient.class);
        OphProperties properties = mock(OphProperties.class);
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_PAIVAKOTI_PROPERTY)).thenReturn(PK_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_PERHEPAIVAHOITAJA_PROPERTY)).thenReturn(PPH_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_RYHMAPERHEPAIVAKOTI_PROPERTY)).thenReturn(RPPH_RYHMA_ID.toString());
        when(properties.getProperty(KAYTTOOIKEUSRYHMA_JOTPA_PROPERTY)).thenReturn(JOTPA_RYHMA_ID.toString());
        OrganisaatioKayttajaFinalizer finalizer = new OrganisaatioKayttajaFinalizer(kayttooikeusClient, properties);

        Rekisterointi rekisterointi = TestiRekisterointi.validiJotpaRekisterointi().withPaatos(
                new Paatos(true, LocalDateTime.now(), "123", null));
        finalizer.kutsuKayttaja(rekisterointi);
        verify(kayttooikeusClient).kutsuKayttaja(
                rekisterointi.paatos.paattaja,
                rekisterointi.kayttaja,
                rekisterointi.organisaatio.oid,
                JOTPA_RYHMA_ID,
                "JOTPA"
        );
    }
}
