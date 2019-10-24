package fi.vm.sade.varda.rekisterointi.configuration;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import fi.vm.sade.varda.rekisterointi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static com.github.kagkarlsson.scheduler.task.schedule.Schedules.parseSchedule;

@Configuration
@RequiredArgsConstructor
public class SchedulingConfiguration {

    private final EmailService emailService;

    @Bean
    public Task<Long> rekisterointiEmailTask() {
        return Tasks.oneTime("rekisterointi-email-task", Long.class).execute((instance, ctx)
                -> emailService.lahetaRekisterointiEmail(instance.getData()));
    }

    @Bean
    public Task<Long> paatosEmailTask() {
        return Tasks.oneTime("paatos-email-task", Long.class).execute((instance, ctx)
                -> emailService.lahetaPaatosEmail(instance.getData()));
    }

    @Bean
    @ConditionalOnProperty("varda-rekisterointi.schedule.kunta-email-task")
    public Task<Void> kuntaEmailTask(Environment environment) {
        String scheduleString = environment.getRequiredProperty("varda-rekisterointi.schedule.kunta-email-task");
        return Tasks.recurring("kunta-email-task", parseSchedule(scheduleString))
                .execute((instance, ctx) -> emailService.lahetaKuntaEmail());
    }

}
