package fi.vm.sade.organisaatio.config.scheduling;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.github.kagkarlsson.scheduler.SchedulerClient;

import fi.vm.sade.organisaatio.email.OsoitepalveluEmailRetryTask;

@Configuration
public class SchedulingClientConfiguration {
    @Bean
    SchedulerClient schedulerClient(DataSource dataSource,
                        VanhentuneetTiedotSahkopostiTask vanhentuneetTiedotSahkopostiTask,
                        KoodistoUpdateTask koodistoUpdateTask,
                        FetchKoodistotTask fetchKoodistotTask,
                        FetchKoulutusluvatTask fetchKoulutusluvatTask,
                        OsoitepalveluEmailRetryTask osoitepalveluEmailRetryTask,
                        @Lazy OrganisaatioUpdateTask organisaatioUpdateTask) {
        return SchedulerClient
            .Builder
            .create(dataSource, koodistoUpdateTask, vanhentuneetTiedotSahkopostiTask, organisaatioUpdateTask, fetchKoodistotTask, fetchKoulutusluvatTask, osoitepalveluEmailRetryTask)
            .build();
    }
}
