package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RekisterointiFinalizerTest {

    private static final Kayttaja TESTI_KAYTTAJA = Kayttaja.builder()
            .asiointikieli("fi")
            .etunimi("Testi")
            .sukunimi("Henkilo")
            .sahkoposti("testi.henkilo@osoite.foo")
            .build();

    @MockitoBean
    private RekisterointiRepository rekisterointiRepository;
    @MockitoBean
    private RekisterointiOrganisaatioFinalizer rekisterointiOrgFinalizer;
    @MockitoBean
    private OrganisaatioKayttajaFinalizer kayttajaFinalizer;
    @MockitoBean
    private SchedulerClient schedulerClient;
    @MockitoBean(name = "kutsuKayttajaTask")
    private Task<Long> kutsuKayttajaTask;
    @MockitoBean(name = "paatosEmailTask")
    private Task<Long> paatosEmailTask;
    @Autowired
    private RekisterointiFinalizer rekisterointiFinalizer;

    @Test
    public void luoTaiPaivitaOrganisaatioTallentaaOidinJaAjastaaKutsun() {
        Organisaatio organisaatio = TestiOrganisaatio.organisaatio(null);
        Rekisterointi rekisterointi = Rekisterointi.of(
                organisaatio,
                "varda",
                "vardatoimintamuoto_tm01",
                Set.of("kunta_123"),
                Set.of("testi@osoite.foo"),
                TESTI_KAYTTAJA);
        when(rekisterointiRepository.findById(anyLong())).thenReturn(Optional.of(rekisterointi));
        when(rekisterointiOrgFinalizer.luoTaiPaivitaOrganisaatio(any(Rekisterointi.class))).thenReturn("1.23.456");
        rekisterointiFinalizer.luoTaiPaivitaOrganisaatio(1L);
        verify(rekisterointiRepository).save(any(Rekisterointi.class));
        verify(kutsuKayttajaTask).instance(anyString(), anyLong());
        verify(schedulerClient).scheduleIfNotExists(any(), any());
    }

    @Test
    public void luoTaiPaivitaOrganisaatioAjastaaKutsun() {
        Organisaatio organisaatio = TestiOrganisaatio.organisaatio("1.23.456");
        Rekisterointi rekisterointi = Rekisterointi.of(
                organisaatio,
                "varda",
                "vardatoimintamuoto_tm01",
                Set.of("kunta_123"),
                Set.of("testi@osoite.foo"),
                TESTI_KAYTTAJA);
        when(rekisterointiRepository.findById(anyLong())).thenReturn(Optional.of(rekisterointi));
        when(rekisterointiOrgFinalizer.luoTaiPaivitaOrganisaatio(any(Rekisterointi.class)))
                .thenReturn(organisaatio.oid);
        rekisterointiFinalizer.luoTaiPaivitaOrganisaatio(1L);
        verify(rekisterointiRepository, never()).save(any(Rekisterointi.class));
        verify(kutsuKayttajaTask).instance(anyString(), anyLong());
        verify(schedulerClient).scheduleIfNotExists(any(), any());
    }

    @Test
    public void kutsuKayttajaAjastaaEmailin() {
        Organisaatio organisaatio = TestiOrganisaatio.organisaatio(null);
        Rekisterointi rekisterointi = Rekisterointi.of(
                organisaatio,
                "varda",
                "vardatoimintamuoto_tm01",
                Set.of("kunta_123"),
                Set.of("testi@osoite.foo"),
                TESTI_KAYTTAJA);
        when(rekisterointiRepository.findById(anyLong())).thenReturn(Optional.of(rekisterointi));
        rekisterointiFinalizer.kutsuKayttaja(1L);
        verify(kayttajaFinalizer).kutsuKayttaja(rekisterointi);
        verify(paatosEmailTask).instance(anyString(), anyLong());
        verify(schedulerClient).scheduleIfNotExists(any(), any());
    }

}
