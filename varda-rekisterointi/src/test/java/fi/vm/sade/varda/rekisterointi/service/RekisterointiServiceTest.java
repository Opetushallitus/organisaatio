package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.PaatosRepository;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class RekisterointiServiceTest {

    @TestConfiguration
    static class RekisterointiServiceTestConfiguration {
        @Bean
        public RekisterointiService rekisterointiService(RekisterointiRepository rekisterointiRepository,
                                                         PaatosRepository paatosRepository,
                                                         ApplicationEventPublisher eventPublisher,
                                                         SchedulerClient schedulerClient, Task<Long> task,
                                                         RekisterointiFinalizer rekisterointiFinalizer) {
            return new RekisterointiService(rekisterointiRepository, paatosRepository, eventPublisher, schedulerClient,
                    task, task, rekisterointiFinalizer);
        }
    }

    private static final Long SAVED_REKISTEROINTI_ID = 1L;
    private static final Long INVALID_REKISTEROINTI_ID = 2L;
    private static final String PAATTAJA_OID = "1.234.56789";
    private static final Rekisterointi SAVED_REKISTEROINTI = new Rekisterointi(
            SAVED_REKISTEROINTI_ID,
            Organisaatio.of(
                    "0000000-0",
                    null,
                    LocalDate.now(),
                    KielistettyNimi.of(
                            "fi", "Testiyritys", null
                    ),
                    "yritysmuoto_26",
                    Set.of("organisaatiotyyppi_07"),
                    "kunta_091",
                    "maatjavaltiot1_fin",
                    Set.of("opetuskieli"),
                    Yhteystiedot.of(
                            "+358101234567",
                            "testi@testiyritys.fi",
                            Osoite.TYHJA, Osoite.TYHJA
                    )),
            "vardatoimintamuoto_tm01",
            Set.of("kunta_091"),
            Set.of("testi.henkilo@foo.bar"),
            Kayttaja.builder()
                    .etunimi("Testi")
                    .sukunimi("Henkilö")
                    .sahkoposti("testi.henkilo@foo.bar")
                    .asiointikieli("fi")
                    .build(),
            LocalDateTime.now(),
            Rekisterointi.Tila.KASITTELYSSA
    );
    private static final Paatos SAVED_PAATOS = new Paatos(
            SAVED_REKISTEROINTI_ID,
            false,
            LocalDateTime.now(),
            PAATTAJA_OID,
            null
    );

    @MockBean
    private RekisterointiRepository rekisterointiRepository;

    @MockBean
    private PaatosRepository paatosRepository;

    @MockBean
    private SchedulerClient schedulerClient;

    @MockBean
    private Task<Long> taskWithLongData;

    @MockBean
    private RekisterointiFinalizer rekisterointiFinalizer;

    @Autowired
    private RekisterointiService rekisterointiService;

    @Before
    public void mockRepositoryCalls() {
        when(rekisterointiRepository.save(any(Rekisterointi.class)))
                .thenReturn(SAVED_REKISTEROINTI.withId(SAVED_REKISTEROINTI_ID));
        when(paatosRepository.save(any(Paatos.class)))
                .thenReturn(SAVED_PAATOS);
        when(rekisterointiRepository.findById(SAVED_REKISTEROINTI_ID))
                .thenReturn(Optional.of(SAVED_REKISTEROINTI));
        when(rekisterointiRepository.findById(INVALID_REKISTEROINTI_ID))
                .thenReturn(Optional.empty());
    }

    @Test
    public void createSavesRekisterointi() {
        Long id = rekisterointiService.create(SAVED_REKISTEROINTI, requestContext("user1"));
        assertEquals(SAVED_REKISTEROINTI_ID, id);
        verify(rekisterointiRepository).save(SAVED_REKISTEROINTI);
    }

    @Test(expected = InvalidInputException.class)
    public void resolveThrowsOnInvalidRekisterointiId() {
        PaatosDto paatos = new PaatosDto(
                INVALID_REKISTEROINTI_ID,
                true,
                "Miksipä ei?"
        );
        rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
    }

    @Test(expected = IllegalStateException.class)
    public void resolveThrowsOnInvalidRekisterointiTila() {
        Rekisterointi hylatty = TestiRekisterointi.validiRekisterointi()
                .withId(123L)
                .withTila(Rekisterointi.Tila.HYLATTY);
        PaatosDto paatos = new PaatosDto(hylatty.id, true, "Juuh elikkäs");
        when(rekisterointiRepository.findById(hylatty.id)).thenReturn(Optional.of(hylatty));
        rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
    }

    @Test
    public void resolveSavesPaatosAndUpdatesRekisterointiTila() {
        PaatosDto paatos = new PaatosDto(
                SAVED_REKISTEROINTI_ID,
                false,
                "Rekisteröinti tehty 110% väärin."
        );
        Rekisterointi expected = SAVED_REKISTEROINTI.withTila(Rekisterointi.Tila.HYLATTY);
        rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
        verify(rekisterointiRepository).save(expected);
        verify(paatosRepository).save(any(Paatos.class));
    }

    @Test
    public void resolveBatchSavesEachPaatosAndUpdatesRekisterointiTila() {
        List<Long> hakemusTunnukset = List.of(1L, 1L);
        PaatosBatch paatokset = new PaatosBatch(
                true,
                "OK!",
                hakemusTunnukset
        );
        Rekisterointi expected = SAVED_REKISTEROINTI.withTila(Rekisterointi.Tila.HYVAKSYTTY);
        rekisterointiService.resolveBatch(PAATTAJA_OID, paatokset, requestContext(null));
        verify(rekisterointiRepository, times(hakemusTunnukset.size())).save(expected);
        verify(paatosRepository, times(hakemusTunnukset.size())).save(any(Paatos.class));
    }

    private RequestContext requestContext(String userId) {
        if (userId == null) {
            return new RequestContextImpl("127.0.0.1");
        }
        return new RequestContextImpl(userId, "127.0.0.1");
    }
}
