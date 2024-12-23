package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Palvelu hyväksytyn rekisteröinnin käsittelemiseen.
 */
@Service
@AllArgsConstructor
public class RekisterointiFinalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiFinalizer.class);
    private static final long ORGANISAATIO_CACHE_KLUDGE_MINUUTIT = 15;

    private final RekisterointiRepository rekisterointiRepository;
    private final RekisterointiOrganisaatioFinalizer rekisterointiOrgFinalizer;
    private final OrganisaatioKayttajaFinalizer organisaatioKayttajaFinalizer;
    private final SchedulerClient schedulerClient;
    @Qualifier("kutsuKayttajaTask")
    private final Task<Long> kutsuKayttajaTask;
    @Qualifier("paatosEmailTask")
    private final Task<Long> paatosEmailTask;

    /**
     * Luo tai päivittää organisaation, ja ajastaa käyttäjän kutsumisen.
     *
     * @param rekisterointiId hyväksytyn rekisteröintihakemuksen tunnus
     */
    public void luoTaiPaivitaOrganisaatio(Long rekisterointiId) {
        Rekisterointi rekisterointi = lataaRekisterointi(rekisterointiId);
        String oid = rekisterointiOrgFinalizer.luoTaiPaivitaOrganisaatio(rekisterointi);
        if (rekisterointi.organisaatio.oid == null) {
            LOGGER.info("Tallennetaan rekisteröintiin luodun organisaation oid: {}", oid);
            rekisterointiRepository.save(rekisterointi.withOrganisaatio(rekisterointi.organisaatio.withOid(oid)));
        }
        schedulerClient.scheduleIfNotExists(
                kutsuKayttajaTask.instance(taskId(kutsuKayttajaTask, rekisterointiId), rekisterointiId),
                Instant.now().plus(ORGANISAATIO_CACHE_KLUDGE_MINUUTIT, ChronoUnit.MINUTES)
        );
    }

    /**
     * Luo käyttäjäkutsun ja ajastaa sähköpostiviestin päätöksestä.
     *
     * @param rekisterointiId hyväksytyn rekisteröintihakemuksen tunnus.
     */
    public void kutsuKayttaja(Long rekisterointiId) {
        Rekisterointi rekisterointi = lataaRekisterointi(rekisterointiId);
        organisaatioKayttajaFinalizer.kutsuKayttaja(rekisterointi);
        ajastaPaatosEmail(rekisterointiId);
    }

    private Rekisterointi lataaRekisterointi(Long rekisterointiId) {
        return rekisterointiRepository.findById(rekisterointiId).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + rekisterointiId)
        );
    }

    private String taskId(Task<Long> task, Long rekisterointiId) {
        return String.format("%s-%d", task.getName(), rekisterointiId);
    }

    private void ajastaPaatosEmail(Long rekisterointiId) {
        schedulerClient.scheduleIfNotExists(
                paatosEmailTask.instance(taskId(paatosEmailTask, rekisterointiId), rekisterointiId),
                Instant.now()
        );
        LOGGER.info("Päätös-email ajastettu rekisteröinnille {}.", rekisterointiId);
    }
}
