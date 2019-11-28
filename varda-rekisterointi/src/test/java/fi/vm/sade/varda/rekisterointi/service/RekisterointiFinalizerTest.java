package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.varda.rekisterointi.client.KayttooikeusClient;
import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RekisterointiFinalizerTest {

    private static final String PAATTAJA_OID = "1.23.456.7890";

    @Test
    public void luoOrganisaationJaKutsuuKayttajanJosEiOidia() {
        OrganisaatioService organisaatioService = new OrganisaatioService();
        OrganisaatioClient organisaatioClient = mock(OrganisaatioClient.class);
        KayttooikeusClient kayttooikeusClient = mock(KayttooikeusClient.class);
        RekisterointiFinalizer finalizer = createFinalizer(organisaatioService, organisaatioClient, kayttooikeusClient);
        when(organisaatioClient.create(any(OrganisaatioV4Dto.class))).thenReturn(new OrganisaatioV4Dto());

        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        finalizer.finalize(rekisterointi, PAATTAJA_OID);

        verify(organisaatioClient).create(any(OrganisaatioV4Dto.class));
        verify(kayttooikeusClient).kutsuKayttaja(anyString(), any(Kayttaja.class), any(), anyLong());
    }

    @Test
    public void paivittaaVardaTiedot() {
        OrganisaatioService organisaatioService = new OrganisaatioService();
        OrganisaatioClient organisaatioClient = mock(OrganisaatioClient.class);
        RekisterointiFinalizer finalizer = createFinalizer(organisaatioService, organisaatioClient);
        Organisaatio organisaatio = Organisaatio.of("1234567-8", "1-23-456-7890", LocalDate.now(),
                KielistettyNimi.of("Testi", "fi", LocalDate.now()),
                "foo", Set.of(), "foo", "bar", Set.of(),
                Yhteystiedot.of("0101234567", "testi@osoite.foo", Osoite.TYHJA, Osoite.TYHJA));
        OrganisaatioV4Dto dto = organisaatioService.muunnaOrganisaatio(organisaatio);
        when(organisaatioClient.getV4ByOid(anyString())).thenReturn(Optional.of(dto));

        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi().withOrganisaatio(organisaatio);
        finalizer.finalize(rekisterointi, PAATTAJA_OID);

        verify(organisaatioClient).save(any(OrganisaatioV4Dto.class));
        assertTrue(dto.tyypit.contains(RekisterointiFinalizer.VARDA_ORGANISAATIOTYYPPI));
        assertEquals(!RekisterointiFinalizer.VARDA_TOIMINTAMUOTO_PAIVAKOTI.equals(rekisterointi.toimintamuoto), dto.piilotettu);
    }

    private RekisterointiFinalizer createFinalizer(OrganisaatioService organisaatioService,
                                                   OrganisaatioClient organisaatioClient) {
        return createFinalizer(
                organisaatioService, organisaatioClient, mock(KayttooikeusClient.class));
    }

    private RekisterointiFinalizer createFinalizer(OrganisaatioService organisaatioService,
                                                   OrganisaatioClient organisaatioClient,
                                                   KayttooikeusClient kayttooikeusClient) {
        OphProperties properties = mock(OphProperties.class);
        when(properties.getProperty(anyString(), any())).thenReturn("1");
        return new RekisterointiFinalizer(
                organisaatioService, organisaatioClient, kayttooikeusClient, properties);
    }
}
