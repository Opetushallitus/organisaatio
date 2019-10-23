package fi.vm.sade.varda.rekisterointi.config;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!integration-test")
public class SchedulerConfiguration {

    @MockBean
    private SchedulerClient schedulerClient;

}
