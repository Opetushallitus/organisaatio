package fi.vm.sade.varda.rekisterointi.service;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import com.github.kagkarlsson.scheduler.task.Task;
import fi.vm.sade.varda.rekisterointi.RequestContext;
import fi.vm.sade.varda.rekisterointi.dto.RekisterointiAuditDto;
import fi.vm.sade.varda.rekisterointi.event.CreatedEvent;
import fi.vm.sade.varda.rekisterointi.event.UpdatedEvent;
import fi.vm.sade.varda.rekisterointi.exception.InvalidInputException;
import fi.vm.sade.varda.rekisterointi.model.Paatos;
import fi.vm.sade.varda.rekisterointi.model.PaatosBatch;
import fi.vm.sade.varda.rekisterointi.model.PaatosDto;
import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import fi.vm.sade.varda.rekisterointi.repository.PaatosRepository;
import fi.vm.sade.varda.rekisterointi.repository.RekisterointiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Transactional
public class RekisterointiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RekisterointiService.class);

    private final RekisterointiRepository rekisterointiRepository;
    private final PaatosRepository paatosRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SchedulerClient schedulerClient;
    private final Task<Long> rekisterointiEmailTask;
    private final Task<Long> paatosEmailTask;

    public RekisterointiService(RekisterointiRepository rekisterointiRepository,
                                PaatosRepository paatosRepository,
                                ApplicationEventPublisher eventPublisher,
                                SchedulerClient schedulerClient,
                                @Qualifier("rekisterointiEmailTask") Task<Long> rekisterointiEmailTask,
                                @Qualifier("paatosEmailTask") Task<Long> paatosEmailTask) {
        this.rekisterointiRepository = rekisterointiRepository;
        this.paatosRepository = paatosRepository;
        this.eventPublisher = eventPublisher;
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

    public long create(Rekisterointi rekisterointi, RequestContext requestContext) {
        Rekisterointi saved = rekisterointiRepository.save(rekisterointi);
        String taskId = String.format("%s-%d", rekisterointiEmailTask.getName(), saved.id);
        schedulerClient.schedule(rekisterointiEmailTask.instance(taskId, saved.id), Instant.now());
        eventPublisher.publishEvent(new CreatedEvent<>(requestContext, "rekisterointi", saved.id));
        LOGGER.info("Rekisteröinti luotu tunnuksella: {}", saved.id);
        return saved.id;
    }

    public Rekisterointi resolve(String paattajaOid, PaatosDto paatosDto, RequestContext requestContext) {
        Paatos paatos = new Paatos(paatosDto.rekisterointi, paatosDto.hyvaksytty, LocalDateTime.now(),paattajaOid, paatosDto.perustelu);
        Rekisterointi rekisterointi = rekisterointiRepository.findById(paatos.rekisterointi).orElseThrow(
                () -> new InvalidInputException("Rekisteröintiä ei löydy, id: " + paatos.rekisterointi));
        RekisterointiAuditDto auditBeforeDto = new RekisterointiAuditDto(rekisterointi.tila);
        paatosRepository.save(paatos);
        LOGGER.info("Päätös tallennettu rekisteröinnille: {}", rekisterointi.id);
        Rekisterointi saved = rekisterointiRepository.save(
                rekisterointi.withTila(paatos.hyvaksytty ? Rekisterointi.Tila.HYVAKSYTTY : Rekisterointi.Tila.HYLATTY));
        RekisterointiAuditDto auditAfterDto = new RekisterointiAuditDto(saved.tila);
        LOGGER.debug("Rekisteröinnin {} tila päivitetty: {}", saved.id, saved.tila);
        String taskId = String.format("%s-%d", paatosEmailTask.getName(), saved.id);
        schedulerClient.schedule(paatosEmailTask.instance(taskId, saved.id), Instant.now());
        eventPublisher.publishEvent(new UpdatedEvent<>(requestContext, "rekisterointi", saved.id,
                auditBeforeDto, auditAfterDto));
        return saved;
    }

    public void resolveBatch(String paattajaOid, PaatosBatch paatokset, RequestContext requestContext) {
        paatokset.hakemukset.forEach(id -> resolve(
                paattajaOid,
                new PaatosDto(
                        id,
                        paatokset.hyvaksytty,
                        paatokset.perustelu
                ),
                requestContext
        ));
    }

}
