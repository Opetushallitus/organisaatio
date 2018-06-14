package fi.vm.sade.organisaatio.config.scheduling;

import com.github.kagkarlsson.scheduler.Scheduler;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mokki ajastuksen aktivoinnille.
 *
 */
@Configuration
public class SchedulingConfiguration {

    @Bean(destroyMethod = "stop")
    Scheduler scheduler() {
        return Mockito.mock(Scheduler.class);
    }
}
