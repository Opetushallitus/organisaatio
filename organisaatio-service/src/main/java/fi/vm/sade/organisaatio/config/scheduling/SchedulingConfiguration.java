package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Ajastuksen aktivointi.
 *
 * *Task-luokat sisältävät ajastusten konfiguroinnit
 */
@Profile("!dev")
@Configuration
public class SchedulingConfiguration {

    @Bean(destroyMethod = "stop")
    Scheduler scheduler(DataSource dataSource,
                        VanhentuneetTiedotSahkopostiTask vanhentuneetTiedotSahkopostiTask,
                        KoodistoUpdateTask koodistoUpdateTask,
                        FetchKoodistotTask fetchKoodistotTask,
                        @Lazy OrganisaatioUpdateTask organisaatioUpdateTask) {
        Scheduler scheduler = Scheduler.create(dataSource, koodistoUpdateTask)
                .startTasks(vanhentuneetTiedotSahkopostiTask, organisaatioUpdateTask, fetchKoodistotTask)
                .threads(1)
                .build();
        scheduler.start();
        return scheduler;
    }
}
