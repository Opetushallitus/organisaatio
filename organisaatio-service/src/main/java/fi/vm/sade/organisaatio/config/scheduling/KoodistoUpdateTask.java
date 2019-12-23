package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import org.springframework.stereotype.Component;

@Component
public class KoodistoUpdateTask extends OneTimeTask<String> {

    private final OrganisaatioKoodisto organisaatioKoodisto;

    public KoodistoUpdateTask(OrganisaatioKoodisto organisaatioKoodisto) {
        super("koodisto päivitys", String.class);
        this.organisaatioKoodisto = organisaatioKoodisto;
    }

    @Override
    public void executeOnce(TaskInstance<String> taskInstance, ExecutionContext executionContext) {
        organisaatioKoodisto.paivitaKoodisto(taskInstance.getData());
    }

}
