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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
                    "yritysmuoto",
                    Collections.singleton("tyyppi"),
                    "Helsinki",
                    "Suomi",
                    Collections.singleton("kieli")),
            "vardatoimintamuoto_tm01",
            Collections.singleton("Helsinki"),
            Collections.emptySet(),
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
        RequestContext requestContext = new RequestContextImpl("user1", "127.0.0.1");
        Long id = rekisterointiService.create(SAVED_REKISTEROINTI, requestContext);
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
        RequestContextImpl requestContext = new RequestContextImpl("127.0.0.1");
        rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext);
    }

    @Test
    public void resolveSavesPaatosAndUpdatesRekisterointiTila() {
        PaatosDto paatos = new PaatosDto(
                SAVED_REKISTEROINTI_ID,
                false,
                "Rekisteröinti tehty 110% väärin."
        );
        Rekisterointi expected = SAVED_REKISTEROINTI.withTila(Rekisterointi.Tila.HYLATTY);
        RequestContextImpl requestContext = new RequestContextImpl("127.0.0.1");
        rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext);
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
        RequestContextImpl requestContext = new RequestContextImpl("127.0.0.1");
        rekisterointiService.resolveBatch(PAATTAJA_OID, paatokset, requestContext);
        verify(rekisterointiRepository, times(hakemusTunnukset.size())).save(expected);
        verify(paatosRepository, times(hakemusTunnukset.size())).save(any(Paatos.class));
    }
}
