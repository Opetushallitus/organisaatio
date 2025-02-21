package fi.vm.sade.varda.rekisterointi;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.SchedulerClient;

@Configuration
public class SchedulerConfiguration {

    @Bean(destroyMethod = "stop")
    Scheduler scheduler() {
        return Mockito.mock(Scheduler.class);
    }

    @Bean
    SchedulerClient schedulerClient() {
        return Mockito.mock(SchedulerClient.class);
    }
}
