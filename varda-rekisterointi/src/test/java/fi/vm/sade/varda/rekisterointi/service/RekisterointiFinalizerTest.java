package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RekisterointiFinalizerTest {

    @Test
    public void luoOrganisaationJosEiOidia() {
        OrganisaatioService service = new OrganisaatioService();
        OrganisaatioClient client = mock(OrganisaatioClient.class);
        RekisterointiFinalizer finalizer = new RekisterointiFinalizer(service, client);
        when(client.create(any(OrganisaatioV4Dto.class))).thenReturn(new OrganisaatioV4Dto());

        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi();
        finalizer.finalize(rekisterointi);

        verify(client).create(any(OrganisaatioV4Dto.class));
    }

    @Test
    public void lisaaVardaToimintamuodon() {
        OrganisaatioService service = new OrganisaatioService();
        OrganisaatioClient client = mock(OrganisaatioClient.class);
        RekisterointiFinalizer finalizer = new RekisterointiFinalizer(service, client);
        Organisaatio organisaatio = Organisaatio.of("1234567-8", "1-23-456-7890", LocalDate.now(),
                KielistettyNimi.of("Testi", "fi", LocalDate.now()),
                "foo", Set.of(), "foo", "bar", Set.of());
        OrganisaatioV4Dto dto = service.muunnaOrganisaatio(organisaatio);
        when(client.getV4ByOid(anyString())).thenReturn(Optional.of(dto));

        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi().withOrganisaatio(organisaatio);
        finalizer.finalize(rekisterointi);

        verify(client).save(any(OrganisaatioV4Dto.class));
        assertTrue(dto.tyypit.contains(RekisterointiFinalizer.VARDA_TOIMINTAMUOTO));
    }
}
