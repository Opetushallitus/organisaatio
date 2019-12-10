package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.OrganisaatioClient;
import fi.vm.sade.varda.rekisterointi.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VardaOrganisaatioFinalizerTest {

    private OrganisaatioService organisaatioService;
    private OrganisaatioClient organisaatioClient;
    private VardaOrganisaatioFinalizer finalizer;

    @Before
    public void setup() {
        organisaatioService = mock(OrganisaatioService.class);
        organisaatioClient = mock(OrganisaatioClient.class);
        finalizer = new VardaOrganisaatioFinalizer(organisaatioService, organisaatioClient);
    }

    @Test
    public void luoOrganisaationJosOidNull() {
        Organisaatio organisaatio = TestiOrganisaatio.organisaatio(null);
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi().withOrganisaatio(organisaatio);
        when(organisaatioService.muunnaOrganisaatio(organisaatio)).thenReturn(new OrganisaatioV4Dto());
        when(organisaatioClient.create(any(OrganisaatioV4Dto.class))).thenReturn(new OrganisaatioV4Dto());
        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        verify(organisaatioService).muunnaOrganisaatio(organisaatio);
        verify(organisaatioClient).create(any(OrganisaatioV4Dto.class));
    }

    @Test
    public void paivittaaOrganisaatioTyypinJosPuuttuu() {
        String oid = "1.23.456";
        Organisaatio organisaatio = TestiOrganisaatio.organisaatio(oid);
        Rekisterointi rekisterointi = TestiRekisterointi.validiRekisterointi().withOrganisaatio(organisaatio);
        OrganisaatioV4Dto dto = new OrganisaatioV4Dto();
        dto.tyypit = Set.of();
        when(organisaatioClient.getV4ByOid(oid)).thenReturn(Optional.of(dto));
        ArgumentCaptor<OrganisaatioV4Dto> captor = ArgumentCaptor.forClass(OrganisaatioV4Dto.class);
        when(organisaatioClient.save(captor.capture())).thenReturn(dto);

        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        assertTrue(captor.getValue().tyypit.contains(VardaOrganisaatioFinalizer.VARDA_ORGANISAATIOTYYPPI));
    }
}
