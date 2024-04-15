package fi.vm.sade.organisaatio.export;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskWithoutDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ExportTaskConfiguration {
    private final ExportService exportService;

    @Bean
    @ConditionalOnProperty(name = "organisaatio.tasks.export.enabled", matchIfMissing = false)
    Task<Void> createSchemaTask() {
        log.info("Creating organisaatio export task");
        return Tasks.recurring(new TaskWithoutDataDescriptor("Data export: create schema"), FixedDelay.ofHours(1))
                .execute((taskInstance, executionContext) -> {
                    log.info("Running organisaatio export task");
                    exportService.createSchema();
                    log.info("Organisaatio export task completed");
                });
    }
}
