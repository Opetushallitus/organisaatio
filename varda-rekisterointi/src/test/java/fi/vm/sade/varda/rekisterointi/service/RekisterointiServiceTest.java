package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.util.RequestContextImpl;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.*;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class RekisterointiServiceTest {

        @TestConfiguration
        static class RekisterointiServiceTestConfiguration {
                @Bean
                public RekisterointiService rekisterointiService(RekisterointiRepository rekisterointiRepository,
                                ApplicationEventPublisher eventPublisher,
                                SchedulerClient schedulerClient,
                                @Qualifier("rekisterointiEmailTask") Task<Long> rekisterointiEmailTask,
                                @Qualifier("paatosEmailTask") Task<Long> paatosEmailTask,
                                @Qualifier("luoTaiPaivitaOrganisaatioTask") Task<Long> luoTaiPaivitaOrganisaatioTask) {
                        return new RekisterointiService(rekisterointiRepository, eventPublisher, schedulerClient,
                                        rekisterointiEmailTask, paatosEmailTask, luoTaiPaivitaOrganisaatioTask);
                }
        }

        private static final Long SAVED_REKISTEROINTI_ID = 1L;
        private static final Long INVALID_REKISTEROINTI_ID = 2L;
        private static final String PAATTAJA_OID = "1.234.56789";
        private static final Rekisterointi SAVED_REKISTEROINTI = getRekisterointi();

        @MockBean
        private RekisterointiRepository rekisterointiRepository;

        @MockBean
        private SchedulerClient schedulerClient;

        @MockBean(name = "rekisterointiEmailTask")
        Task<Long> rekisterointiEmailTask;

        @MockBean(name = "paatosEmailTask")
        Task<Long> paatosEmailTask;

        @MockBean(name = "luoTaiPaivitaOrganisaatioTask")
        Task<Long> luoTaiPaivitaOrganisaatioTask;

        @Autowired
        private RekisterointiService rekisterointiService;

        @Before
        public void mockRepositoryCalls() {
                when(rekisterointiRepository.save(any(Rekisterointi.class)))
                                .thenReturn(SAVED_REKISTEROINTI.withId(SAVED_REKISTEROINTI_ID));
                when(rekisterointiRepository.findById(SAVED_REKISTEROINTI_ID))
                                .thenReturn(Optional.of(SAVED_REKISTEROINTI));
                when(rekisterointiRepository.findById(INVALID_REKISTEROINTI_ID))
                                .thenReturn(Optional.empty());
        }

        @Test
        public void createSavesRekisterointi() {
                Long id = rekisterointiService.create(SAVED_REKISTEROINTI, requestContext("user1"));
                assertEquals(SAVED_REKISTEROINTI_ID, id);
                assertFalse(SAVED_REKISTEROINTI.organisaatio.uudelleenRekisterointi);
                verify(rekisterointiRepository).save(SAVED_REKISTEROINTI);
        }

        @Test
        public void createSavesUudelleenRekisterointi() {
                when(rekisterointiRepository.findByYtunnus(SAVED_REKISTEROINTI.organisaatio.ytunnus))
                                .thenReturn(List.of(SAVED_REKISTEROINTI));

                Rekisterointi rekisterointi = getRekisterointi();
                Long id = rekisterointiService.create(rekisterointi, requestContext("user1"));
                assertEquals(SAVED_REKISTEROINTI_ID, id);
                assertTrue(rekisterointi.organisaatio.uudelleenRekisterointi);
                verify(rekisterointiRepository).save(rekisterointi);
        }

        @Test(expected = InvalidInputException.class)
        public void resolveThrowsOnInvalidRekisterointiId() {
                PaatosDto paatos = new PaatosDto(
                                INVALID_REKISTEROINTI_ID,
                                true,
                                "Miksipä ei?");
                rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
        }

        @Test(expected = IllegalStateException.class)
        public void resolveThrowsOnInvalidRekisterointiTila() {
                PaatosDto paatos = new PaatosDto(123L, false, "Juuh elikkäs");
                Rekisterointi hylatty = TestiRekisterointi.validiRekisterointi()
                                .withId(paatos.rekisterointi)
                                .withPaatos(new Paatos(paatos.hyvaksytty, LocalDateTime.now(), PAATTAJA_OID,
                                                paatos.perustelu));
                when(rekisterointiRepository.findById(hylatty.id)).thenReturn(Optional.of(hylatty));
                rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
        }

        @Test
        public void resolveSavesPaatosAndUpdatesRekisterointiTila() {
                PaatosDto paatos = new PaatosDto(
                                SAVED_REKISTEROINTI_ID,
                                false,
                                "Rekisteröinti tehty 110% väärin.");
                rekisterointiService.resolve(PAATTAJA_OID, paatos, requestContext(null));
                ArgumentCaptor<Rekisterointi> captor = ArgumentCaptor.forClass(Rekisterointi.class);
                verify(rekisterointiRepository).save(captor.capture());
                assertEquals(Rekisterointi.Tila.HYLATTY, captor.getValue().tila);
        }

        @Test
        public void resolveBatchSavesEachPaatosAndUpdatesRekisterointiTila() {
                List<Long> hakemusTunnukset = List.of(1L, 1L);
                PaatosBatch paatokset = new PaatosBatch(
                                true,
                                "OK!",
                                hakemusTunnukset);
                rekisterointiService.resolveBatch(PAATTAJA_OID, paatokset, requestContext(null));
                ArgumentCaptor<Rekisterointi> captor = ArgumentCaptor.forClass(Rekisterointi.class);
                verify(rekisterointiRepository, times(hakemusTunnukset.size())).save(captor.capture());
                assertEquals(Rekisterointi.Tila.HYVAKSYTTY, captor.getValue().tila);
        }

        private RequestContext requestContext(String userId) {
                if (userId == null) {
                        return new RequestContextImpl("127.0.0.1");
                }
                return new RequestContextImpl(userId, "127.0.0.1");
        }

        private static Rekisterointi getRekisterointi() {
                return new Rekisterointi(
                                SAVED_REKISTEROINTI_ID,
                                Organisaatio.of(
                                                "0000000-0",
                                                null,
                                                LocalDate.now(),
                                                KielistettyNimi.of(
                                                                "fi", "Varda-yritys", null),
                                                "yritysmuoto_26",
                                                Set.of("organisaatiotyyppi_07"),
                                                "kunta_091",
                                                "maatjavaltiot1_fin",
                                                Set.of("opetuskieli"),
                                                Yhteystiedot.of(
                                                                "+358101234567",
                                                                "testi@testiyritys.fi",
                                                                Osoite.TYHJA, Osoite.TYHJA),
                                                false),
                                "varda",
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
                                null,
                                Rekisterointi.Tila.KASITTELYSSA);
        }
}
