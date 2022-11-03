package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Laukaisee organisaatioiden ajastetut päivitys operaatiot
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrganisaatioUpdateTask  {


    private final OrganisaatioBusinessService organisaatioBusinessService;

    private final OrganisaatioYtjService organisaatioYtjService;



    @Scheduled(cron = "${organisaatio-service.scheduled.organisaatio_update_task.cron.expression:0 0 1 * * ?}")
    public void executeRecurringly() {
        log.info("Running scheduled {}", this.getClass().getName());
        organisaatioBusinessService.updateCurrentOrganisaatioNimet();
        organisaatioBusinessService.processNewOrganisaatioSuhdeChanges();
        organisaatioYtjService.updateYTJData(false);
    }
}
