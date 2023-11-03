package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@Slf4j
public class VanhentuneetTiedotSahkopostiTask extends RecurringTask<Void> {

    private final VanhentuneetTiedotSahkopostiService service;

    private final AuthenticationUtil authenticationUtil;

    public VanhentuneetTiedotSahkopostiTask(VanhentuneetTiedotSahkopostiService service,
                                            AuthenticationUtil authenticationUtil) {
        super("vanhentuneet tiedot sähköposti", new Weekdays(LocalTime.of(8, 0)), Void.class, null);
        this.service = service;
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            service.lahetaSahkopostit();
        } catch (Exception e) {
            log.info("VanhentuneetTiedotSahkopostiTask failed with exception", e);
            throw e;
        } finally {
            MDC.remove("requestId");
        }
    }

}
