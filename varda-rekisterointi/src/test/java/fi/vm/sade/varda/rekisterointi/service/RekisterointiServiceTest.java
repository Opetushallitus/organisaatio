package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
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
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RekisterointiServiceTest {

    @TestConfiguration
    static class RekisterointiServiceTestConfiguration {
        @Bean
        public RekisterointiService rekisterointiService(RekisterointiRepository rekisterointiRepository, PaatosRepository paatosRepository,
                                                         SchedulerClient schedulerClient, Task<Long> task) {
            return new RekisterointiService(rekisterointiRepository, paatosRepository, schedulerClient, task, task);
        }
    }

    private static final Long SAVED_REKISTEROINTI_ID = 1L;
    private static final Long INVALID_REKISTEROINTI_ID = 2L;
    private static final Rekisterointi SAVED_REKISTEROINTI = new Rekisterointi(
            SAVED_REKISTEROINTI_ID,
            Organisaatio.of(
                    "0000000-0",
                    null,
                    LocalDate.now(),
                    KielistettyNimi.of(
                            "fi", "Testiyritys", null
                    ),
                    "toimintamuoto",
                    Collections.singleton("tyyppi"),
                    "Helsinki",
                    "Suomi"),
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
            1L,
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
        Long id = rekisterointiService.create(SAVED_REKISTEROINTI);
        assertEquals(SAVED_REKISTEROINTI_ID, id);
        verify(rekisterointiRepository).save(SAVED_REKISTEROINTI);
    }

    @Test(expected = InvalidInputException.class)
    public void resolveThrowsOnInvalidRekisterointiId() {
        Paatos paatos = new Paatos(
                INVALID_REKISTEROINTI_ID,
                true,
                LocalDateTime.now(),
                1L,
                "Miksipä ei?"
        );
        rekisterointiService.resolve(paatos);
    }

    @Test
    public void resolveSavesPaatosWithRekisterointi() {
        Paatos paatos = new Paatos(
                SAVED_REKISTEROINTI_ID,
                false,
                LocalDateTime.now(),
                1L,
                "Rekisteröinti tehty 110% väärin."
        );
        Rekisterointi expected = SAVED_REKISTEROINTI.withTila(Rekisterointi.Tila.HYLATTY);
        rekisterointiService.resolve(paatos);
        verify(rekisterointiRepository).save(expected);
    }
}
