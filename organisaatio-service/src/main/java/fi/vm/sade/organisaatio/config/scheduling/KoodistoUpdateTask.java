package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import fi.vm.sade.organisaatio.business.OrganisaatioKoodisto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KoodistoUpdateTask extends OneTimeTask<String> {

    private final OrganisaatioKoodisto organisaatioKoodisto;

    public KoodistoUpdateTask(OrganisaatioKoodisto organisaatioKoodisto) {
        super("koodisto päivitys", String.class);
        this.organisaatioKoodisto = organisaatioKoodisto;
    }

    @Override
    public void executeOnce(TaskInstance<String> taskInstance, ExecutionContext executionContext) {
        log.info("Running kagkarlsson scheduled {}", this.getClass().getName());
        organisaatioKoodisto.paivitaKoodisto(taskInstance.getData());
    }

}
