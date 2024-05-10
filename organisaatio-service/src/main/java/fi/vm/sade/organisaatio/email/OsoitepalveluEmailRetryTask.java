package fi.vm.sade.organisaatio.email;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import fi.vm.sade.organisaatio.config.scheduling.AuthenticationUtil;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;


@Component
@Slf4j
public class OsoitepalveluEmailRetryTask extends RecurringTask<Void> {
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationUtil authenticationUtil;

    public OsoitepalveluEmailRetryTask() {
        super("OsoitepalveluEmailRetryTask", FixedDelay.of(Duration.ofMinutes(5)), Void.class, (FailureHandler<Void>) null);
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            execute();
        } catch (Exception e) {
            log.info("OsoitepalveluEmailRetryTask failed with exception", e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }


    private void execute() {
        emailService.getQueuedEmailsToRetry().forEach(email -> {
            try {
                emailService.attemptSendingEmail(email.getId());
            } catch (Exception e) {
                log.info("Failed to send email " + email.getId(), e);
            }
        });
    }
}
