package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import fi.vm.sade.organisaatio.model.listeners.ProtectedDataListener;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class KoodistoUpdateTask extends OneTimeTask<String> {

    private final OrganisaatioKoodisto organisaatioKoodisto;

    private final AuthenticationUtil authenticationUtil;

    public KoodistoUpdateTask(OrganisaatioKoodisto organisaatioKoodisto,
                              AuthenticationUtil authenticationUtil) {
        super("koodisto päivitys", String.class);
        this.organisaatioKoodisto = organisaatioKoodisto;
        this.authenticationUtil = authenticationUtil;
    }

    @Override
    public void executeOnce(TaskInstance<String> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            authenticationUtil.configureAuthentication(ProtectedDataListener.ROLE_CRUD_OPH);
            organisaatioKoodisto.paivitaKoodisto(taskInstance.getData());
        } finally {
            MDC.remove("requestId");
        }
    }

}
