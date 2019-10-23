package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.repository.PaatosRepository;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class RekisterointiService {

    private final RekisterointiRepository rekisterointiRepository;
    private final PaatosRepository paatosRepository;
    private final SchedulerClient schedulerClient;
    private final Task<Long> rekisterointiEmailTask;
    private final Task<Long> paatosEmailTask;

    public RekisterointiService(RekisterointiRepository rekisterointiRepository,
                                PaatosRepository paatosRepository,
                                SchedulerClient schedulerClient,
                                @Qualifier("rekisterointiEmailTask") Task<Long> rekisterointiEmailTask,
                                @Qualifier("paatosEmailTask") Task<Long> paatosEmailTask) {
        this.rekisterointiRepository = rekisterointiRepository;
        this.paatosRepository = paatosRepository;
        this.schedulerClient = schedulerClient;
        this.rekisterointiEmailTask = rekisterointiEmailTask;
        this.paatosEmailTask = paatosEmailTask;
    }

    public Iterable<Rekisterointi> listByTilaAndOrganisaatio(Rekisterointi.Tila tila, String organisaatio) {
        // TODO: rajaus kunnan/päättäjän perusteella? KJHH-1709
        if (organisaatio == null || organisaatio.length() == 0) {
            return rekisterointiRepository.findByTila(tila.toString());
        }
        return rekisterointiRepository.findByTilaAndOrganisaatioContaining(tila.toString(), organisaatio);
    }

    public long create(Rekisterointi rekisterointi) {
        Rekisterointi saved = rekisterointiRepository.save(rekisterointi);
        String taskId = String.format("%s-%d", rekisterointiEmailTask.getName(), saved.id);
        schedulerClient.schedule(rekisterointiEmailTask.instance(taskId, saved.id), Instant.now());
        return saved.id;
    }

    public Rekisterointi resolve(Paatos paatos) {
        Rekisterointi rekisterointi = rekisterointiRepository.findById(paatos.rekisterointi).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + paatos.rekisterointi));
        paatosRepository.save(paatos);
        Rekisterointi saved = rekisterointiRepository.save(
                rekisterointi.withTila(paatos.hyvaksytty ? Rekisterointi.Tila.HYVAKSYTTY : Rekisterointi.Tila.HYLATTY));
        String taskId = String.format("%s-%d", paatosEmailTask.getName(), saved.id);
        schedulerClient.schedule(paatosEmailTask.instance(taskId, saved.id), Instant.now());
        return saved;
    }

}
