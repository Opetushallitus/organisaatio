package fi.vm.sade.varda.rekisterointi.configuration;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import fi.vm.sade.varda.rekisterointi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

import static com.github.kagkarlsson.scheduler.task.schedule.Schedules.daily;

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
    public Task<Void> kuntaEmailTask() {
        return Tasks.recurring("kunta-email-task", daily(LocalTime.of(6, 0)))
                .execute((instance, ctx) -> emailService.lahetaKuntaEmail());
    }

}
