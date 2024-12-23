package fi.vm.sade.varda.rekisterointi.config;

import com.github.kagkarlsson.scheduler.Scheduler;
import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"!integration-test"})
public class SchedulerConfiguration {

    @MockBean(answer = Answers.RETURNS_MOCKS)
    private Scheduler scheduler;
}
