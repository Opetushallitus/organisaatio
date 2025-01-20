package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import fi.vm.sade.organisaatio.business.OrganisaatioBusinessService;
import fi.vm.sade.organisaatio.business.OrganisaatioYtjService;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Laukaisee organisaatioiden ajastetut päivitys operaatiot
 */
@Component
@Slf4j
public class OrganisaatioUpdateTask extends RecurringTask<Void> {
    private final String nameUpdateCronExpression;

    private final OrganisaatioBusinessService organisaatioBusinessService;

    private final OrganisaatioYtjService organisaatioYtjService;

    private final AuthenticationUtil authenticationUtil;

    public OrganisaatioUpdateTask(@Lazy OrganisaatioBusinessService organisaatioBusinessService,
                                  @Lazy OrganisaatioYtjService organisaatioYtjService,
                                  @Value("${organisaatio-service.scheduled.update.cron.expression:0 0 1 * * ?}") String nameUpdateCronExpression,
                                  AuthenticationUtil authenticationUtil
    ) {
        super("päivittäiset ajastukset task", new Daily(CronExpression.parse(nameUpdateCronExpression).next(LocalDateTime.now()).toLocalTime()), Void.class, (FailureHandler<Void>) null);
        this.organisaatioBusinessService = organisaatioBusinessService;
        this.organisaatioYtjService = organisaatioYtjService;
        this.nameUpdateCronExpression = nameUpdateCronExpression;
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public void executeRecurringly(TaskInstance taskInstancex, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            log.info("Running organisaatio update task");
            log.info("scheduledUpdate(): Cron Expression: {}, Current time: {}", nameUpdateCronExpression, new Date());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            organisaatioBusinessService.updateCurrentOrganisaatioNimet();
            organisaatioBusinessService.processNewOrganisaatioSuhdeChanges();
            organisaatioYtjService.updateYTJData(false);
            log.info("Organisaatio update task completed");
        } catch (Exception e) {
            log.info("OrganisaatioUpdateTask failed with exception", e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }
}
