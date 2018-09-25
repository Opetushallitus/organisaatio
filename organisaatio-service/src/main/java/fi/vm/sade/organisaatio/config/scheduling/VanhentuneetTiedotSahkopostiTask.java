package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import fi.vm.sade.organisaatio.business.impl.VanhentuneetTiedotSahkopostiService;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class VanhentuneetTiedotSahkopostiTask extends RecurringTask<Void> {

    private final VanhentuneetTiedotSahkopostiService service;

    public VanhentuneetTiedotSahkopostiTask(VanhentuneetTiedotSahkopostiService service) {
        super("vanhentuneet tiedot sähköposti", new Daily(LocalTime.of(8, 0)), Void.class, null);
        this.service = service;
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        service.lahetaSahkopostit();
    }

}
