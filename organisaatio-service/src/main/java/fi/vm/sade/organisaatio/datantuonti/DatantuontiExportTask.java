package fi.vm.sade.organisaatio.datantuonti;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(value = "organisaatio.tasks.datantuonti.export.enabled", havingValue = "true")
public class DatantuontiExportTask extends RecurringTask<Void> {
    @Autowired
    private DatantuontiExportService datantuontiExportService;

    public DatantuontiExportTask() {
        super(
                "DatantuontiExportTask",
                FixedDelay.of(Duration.ofHours(1)),
                Void.class,
                new FailureHandler.OnFailureReschedule<>(FixedDelay.of(Duration.ofHours(1)))
        );
        log.info("Creating datantuonti export task");
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            log.info("Running organisaatio datantuonti export task");
            String secondsFromEpoch = datantuontiExportService.createSchemaAndReturnTransactionTimestampFromEpoch();
            datantuontiExportService.generateExportFiles(secondsFromEpoch);
            log.info("Organisaatio datantuonti export task completed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            MDC.remove("requestId");
        }
    }
}
