package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.OrganisaatioV4Dto;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.model.TestiRekisterointi;
import org.junit.Test;

import java.time.LocalDateTime;

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
        Paatos paatos = new Paatos(rekisterointi.id, true, LocalDateTime.now(), "1-23-456-7890", "Test.");
        finalizer.finalize(rekisterointi, paatos);

        verify(client).create(any(OrganisaatioV4Dto.class));
    }
}
