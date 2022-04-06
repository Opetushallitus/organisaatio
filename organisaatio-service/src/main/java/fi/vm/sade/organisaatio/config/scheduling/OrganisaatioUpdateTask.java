package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Laukaisee organisaatioiden ajastetut päivitys operaatiot
 */
@Component
public class OrganisaatioUpdateTask extends RecurringTask<Void> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String nameUpdateCronExpression;

    private final OrganisaatioBusinessService organisaatioBusinessService;

    private final OrganisaatioYtjService organisaatioYtjService;

    public OrganisaatioUpdateTask(@Lazy OrganisaatioBusinessService organisaatioBusinessService,
                                  @Lazy OrganisaatioYtjService organisaatioYtjService,
                                  @Value("${organisaatio-service.scheduled.update.cron.expression:0 0 1 * * ?}")
                                          String nameUpdateCronExpression) {
        super("päivittäiset ajastukset task", new Daily(LocalDateTime.ofInstant(new CronSequenceGenerator(nameUpdateCronExpression).next(new Date()).toInstant(), ZoneId.systemDefault()).toLocalTime()), Void.class, null);

        this.organisaatioBusinessService = organisaatioBusinessService;
        this.organisaatioYtjService = organisaatioYtjService;
        this.nameUpdateCronExpression = nameUpdateCronExpression;

    }

    @Override
    public void executeRecurringly(TaskInstance taskInstance, ExecutionContext executionContext) {
        logger.debug("scheduledUpdate(): Cron Expression: {}, Current time: {}", nameUpdateCronExpression, new Date());

        organisaatioBusinessService.updateCurrentOrganisaatioNimet();
        organisaatioBusinessService.processNewOrganisaatioSuhdeChanges();
        organisaatioYtjService.updateYTJData(false);

    }

}
