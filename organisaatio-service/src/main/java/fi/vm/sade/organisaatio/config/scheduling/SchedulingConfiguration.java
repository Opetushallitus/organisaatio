package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.Scheduler;

import fi.vm.sade.organisaatio.datantuonti.DatantuontiExportTask;
import fi.vm.sade.organisaatio.datantuonti.DatantuontiImportTask;
import fi.vm.sade.organisaatio.email.EmailRetryTask;
import fi.vm.sade.organisaatio.export.ExportTask;
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
                        FetchKoulutusluvatTask fetchKoulutusluvatTask,
                        EmailRetryTask emailRetryTask,
                        ExportTask exportTask,
                        DatantuontiExportTask datanTuontiExportTask,
                        DatantuontiImportTask datantuontiImportTask,
                        @Lazy OrganisaatioUpdateTask organisaatioUpdateTask) {
        Scheduler scheduler = Scheduler.create(dataSource, koodistoUpdateTask)
                .startTasks(vanhentuneetTiedotSahkopostiTask,
                            organisaatioUpdateTask,
                            fetchKoodistotTask,
                            fetchKoulutusluvatTask,
                            emailRetryTask,
                            exportTask,
                            datanTuontiExportTask,
                            datantuontiImportTask)
                .threads(1)
                .build();
        scheduler.start();
        return scheduler;
    }
}
