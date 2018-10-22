package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import fi.vm.sade.organisaatio.business.VanhentuneetTiedotSahkopostiService;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class VanhentuneetTiedotSahkopostiTask extends RecurringTask<Void> {

    private final VanhentuneetTiedotSahkopostiService service;

    public VanhentuneetTiedotSahkopostiTask(VanhentuneetTiedotSahkopostiService service) {
        super("vanhentuneet tiedot sähköposti", new Weekdays(LocalTime.of(8, 0)), Void.class, null);
        this.service = service;
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        service.lahetaSahkopostit();
    }

}
