package fi.vm.sade.organisaatio.datantuonti;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.Daily;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;

import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

@Component
@Slf4j
public class DatantuontiImportTask extends RecurringTask<Void> {
    @Autowired
    private DatantuontiImportService importService;

    @Value("${organisaatio.tasks.datantuonti.import.enabled}")
    private Boolean taskEnabled;

    public DatantuontiImportTask() {
        super(
                "DatantuontiImportTask",
                new Daily(LocalTime.of(2, 0)),
                Void.class,
                new FailureHandler.OnFailureReschedule<>(FixedDelay.of(Duration.ofHours(1)))
        );
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        if (taskEnabled) {
            try {
                MDC.put("requestId", RequestIdFilter.generateRequestId());
                log.info("Running organisaatio datantuonti import task");
                importService.importTempTableFromS3();
                importService.createNewOrganisations();
                importService.saveRyhmaData();
                log.info("Organisaatio datantuonti import task completed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                MDC.remove("requestId");
            }
        } else {
            log.info("Skipping organisaatio datantuonti import since it is not enabled");
        }
    }
}
