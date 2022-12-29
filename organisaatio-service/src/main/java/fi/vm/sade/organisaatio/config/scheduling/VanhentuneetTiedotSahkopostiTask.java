package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
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
        authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
        service.lahetaSahkopostit();
    }

}
