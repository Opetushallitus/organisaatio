package fi.vm.sade.varda.rekisterointi.dao;

import fi.vm.sade.varda.rekisterointi.service.TaskMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ScheduledTaskDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskDao.class);
    private static final String FAILING_TASKS_QUERY =
            "SELECT task_instance FROM scheduled_tasks WHERE task_name = ? AND consecutive_failures > ?";

    private final JdbcTemplate template;

    public ScheduledTaskDao(JdbcTemplate template) {
        this.template = template;
    }

    public Set<TaskMonitoringService.TaskFailure> haeEpaonnistumiset() {
        Set<TaskMonitoringService.TaskFailure> epaonnistumiset = new HashSet<>();
        for (TaskMonitoringService.MonitoredTaskType tyyppi : TaskMonitoringService.MonitoredTaskType.values()) {
            LOGGER.info("Haetaan yli {} kertaa epäonnistuneet taskit tyyppiä: {}",
                    TaskMonitoringService.MAX_ALLOWED_FAILURES, tyyppi.taskName);
            template.query(
                    FAILING_TASKS_QUERY,
                    new Object[] { tyyppi.taskName, TaskMonitoringService.MAX_ALLOWED_FAILURES },
                    (resultSet) -> {
                        String taskInstance =  resultSet.getString("task_instance");
                        epaonnistumiset.add(
                                new TaskMonitoringService.TaskFailure(
                                        tyyppi,
                                        Long.parseLong(taskInstance.substring(taskInstance.lastIndexOf('-') + 1))
                                )
                        ); // parsitaan rekisteröinti: task_instance muodostetaan task_name + rekisteröinnin id
                    }
            );
        }
        return epaonnistumiset;
    }
}
