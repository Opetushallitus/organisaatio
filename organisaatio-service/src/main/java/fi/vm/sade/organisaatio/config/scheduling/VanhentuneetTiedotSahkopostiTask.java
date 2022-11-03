package fi.vm.sade.organisaatio.config.scheduling;

import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VanhentuneetTiedotSahkopostiTask {

    private final VanhentuneetTiedotSahkopostiService service;

    @Scheduled(cron = "${organisaatio-service.scheduled.vanhentuneet_tiedot_sahkoposti_task.cron.expression:0 * * * * *}")
    public void executeRecurringly() {
        log.info("Running scheduled {}", this.getClass().getName());
        service.lahetaSahkopostit();
    }
}
