package fi.vm.sade.organisaatio.export;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.FailureHandler;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import fi.vm.sade.organisaatio.service.filters.RequestIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@Slf4j
public class ExportTask extends RecurringTask<Void> {
    @Autowired
    private ExportService exportService;

    @Value("${organisaatio.tasks.export.copy-to-lampi}")
    private boolean copyToLampi;

    public ExportTask() {
        super("ExportTask", FixedDelay.of(Duration.ofHours(1)), Void.class, (FailureHandler<Void>) null);
        log.info("Creating export task");
    }

    @Override
    public void executeRecurringly(TaskInstance<Void> taskInstance, ExecutionContext executionContext) {
        try {
            MDC.put("requestId", RequestIdFilter.generateRequestId());
            log.info("Running organisaatio export task");
            exportService.createSchema();
            exportService.generateExportFiles();
            if (copyToLampi) {
                exportService.copyExportFilesToLampi();
            } else {
                log.info("Copying export files to Lampi is disabled");
            }
            log.info("Organisaatio export task completed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            MDC.remove("requestId");
        }
    }
}
