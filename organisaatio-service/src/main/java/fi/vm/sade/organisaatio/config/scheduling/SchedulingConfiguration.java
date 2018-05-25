package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Ajastuksen aktivointi.
 *
 * *Task-luokat sisältävät ajastusten konfiguroinnit
 */
@Configuration
public class SchedulingConfiguration {

    @Bean(destroyMethod = "stop")
    Scheduler scheduler(DataSource dataSource,
                        OrganisaatioUpdateTask organisaatioUpdateTask) {
        Scheduler scheduler = Scheduler.create(dataSource)
                .startTasks(organisaatioUpdateTask)
                .threads(1)
                .build();
        scheduler.start();
        return scheduler;
    }
}
