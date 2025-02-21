package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.client.KoodistoClient;
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

public class RekisterointiOrganisaatioFinalizerTest {

    private OrganisaatioService organisaatioService;
    private OrganisaatioClient organisaatioClient;
    private RekisterointiOrganisaatioFinalizer finalizer;

    @Before
    public void setup() {
        organisaatioService = new OrganisaatioService(mock(KoodistoClient.class), mock(OrganisaatioClient.class));
        organisaatioClient = mock(OrganisaatioClient.class);
        finalizer = new RekisterointiOrganisaatioFinalizer(organisaatioService, organisaatioClient);
    }

    @Test
    public void luoOrganisaationJosOidNull() {
        Organisaatio organisaatio = TestiOrganisaatio.jotpaOrganisaatio(null);
        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi().withOrganisaatio(organisaatio);
        when(organisaatioClient.create(any(OrganisaatioDto.class))).thenReturn(new OrganisaatioDto());
        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        verify(organisaatioClient).create(any(OrganisaatioDto.class));
    }

    @Test
    public void paivittaaVardaOrganisaatioTyypinJosPuuttuu() {
        String oid = "1.23.456";
        Organisaatio organisaatio = TestiOrganisaatio.jotpaOrganisaatio(oid);
        Rekisterointi rekisterointi = TestiRekisterointi.validiVardaRekisterointi().withOrganisaatio(organisaatio);
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.tyypit = Set.of();
        when(organisaatioClient.getOrganisaatioByOid(oid)).thenReturn(Optional.of(dto));
        ArgumentCaptor<OrganisaatioDto> captor = ArgumentCaptor.forClass(OrganisaatioDto.class);
        when(organisaatioClient.save(captor.capture())).thenReturn(dto);

        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        assertTrue(captor.getValue().tyypit.contains(RekisterointiOrganisaatioFinalizer.VARDA_ORGANISAATIOTYYPPI));
    }

    @Test
    public void paivittaaJotpaOrganisaatioTyypinJosPuuttuuJaAliorganisaation() {
        String oid = "1.23.456";
        Organisaatio organisaatio = TestiOrganisaatio.jotpaOrganisaatio(oid);
        Rekisterointi rekisterointi = TestiRekisterointi.validiJotpaRekisterointi().withOrganisaatio(organisaatio);
        OrganisaatioDto dto = new OrganisaatioDto();
        dto.tyypit = Set.of();
        when(organisaatioClient.getOrganisaatioByOid(oid)).thenReturn(Optional.of(dto));
        ArgumentCaptor<OrganisaatioDto> captor = ArgumentCaptor.forClass(OrganisaatioDto.class);
        OrganisaatioDto jotpaChild = OrganisaatioDto.jotpaChildOppilaitosFrom(dto);
        when(organisaatioClient.save(captor.capture())).thenReturn(dto, jotpaChild);

        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        assertTrue(captor.getAllValues().get(0).tyypit.contains(RekisterointiOrganisaatioFinalizer.JOTPA_ORGANISAATIOTYYPPI));
        assertTrue(captor.getAllValues().get(1).oppilaitosTyyppiUri.equals("oppilaitostyyppi_xx#1"));
        assertTrue(captor.getAllValues().get(1).tyypit.contains("organisaatiotyyppi_02"));
    }

    @Test
    public void paivittaaOppilaitoksenVaikkaKoulutustoimijaOnAjanTasalla() {
        String oid = "1.23.456";
        Organisaatio organisaatio = TestiOrganisaatio.jotpaOrganisaatio(oid);
        Rekisterointi rekisterointi = TestiRekisterointi.validiJotpaRekisterointi().withOrganisaatio(organisaatio);
        var koulutustoimija = organisaatioService.muunnaOrganisaatio(organisaatio);
        var oppilaitos = OrganisaatioDto.jotpaChildOppilaitosFrom(koulutustoimija);

        ArgumentCaptor<OrganisaatioDto> captor = ArgumentCaptor.forClass(OrganisaatioDto.class);
        when(organisaatioClient.getOrganisaatioByOid(oid)).thenReturn(Optional.of(koulutustoimija));
        when(organisaatioClient.save(captor.capture())).thenReturn(oppilaitos);
        finalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        assertTrue(captor.getAllValues().get(0).oppilaitosTyyppiUri.equals("oppilaitostyyppi_xx#1"));
        assertTrue(captor.getAllValues().get(0).tyypit.contains("organisaatiotyyppi_02"));
    }
}
