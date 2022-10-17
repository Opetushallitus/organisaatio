package fi.vm.sade.varda.rekisterointi.configuration;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import fi.vm.sade.varda.rekisterointi.service.EmailService;
import fi.vm.sade.varda.rekisterointi.service.RekisterointiFinalizer;
import fi.vm.sade.varda.rekisterointi.service.TaskMonitoringService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import static com.github.kagkarlsson.scheduler.task.schedule.Schedules.parseSchedule;

@Configuration
public class SchedulingConfiguration {

    private final EmailService emailService;
    private final TaskMonitoringService taskMonitoringService;
    private final RekisterointiFinalizer rekisterointiFinalizer;

    public SchedulingConfiguration(EmailService emailService,
                                   TaskMonitoringService taskMonitoringService,
                                   @Lazy RekisterointiFinalizer rekisterointiFinalizer) {
        this.emailService = emailService;
        this.taskMonitoringService = taskMonitoringService;
        this.rekisterointiFinalizer = rekisterointiFinalizer;
    }

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

    @Bean
    @ConditionalOnProperty("varda-rekisterointi.schedule.kasittelyssa-email-task")
    public Task<Void> kasittelyssaEmailTask(Environment environment) {
        String scheduleString = environment.getRequiredProperty("varda-rekisterointi.schedule.kasittelyssa-email-task");
        return Tasks.recurring("kasittelyssa-email-task", parseSchedule(scheduleString))
                .execute((instance, ctx) -> emailService.lahetaKasittelyssaEmails());
    }

    @Bean
    public Task<Long> luoTaiPaivitaOrganisaatioTask() {
        return Tasks.oneTime("luo-tai-paivita-organisaatio-task", Long.class).execute(
                (instance, ctx) -> rekisterointiFinalizer.luoTaiPaivitaOrganisaatio(instance.getData())
        );
    }

    @Bean
    public Task<Long> kutsuKayttajaTask() {
        return Tasks.oneTime("kutsu-kayttaja-task", Long.class).execute(
                (instance, ctx) -> rekisterointiFinalizer.kutsuKayttaja(instance.getData())
        );
    }

    @Bean
    @ConditionalOnProperty("varda-rekisterointi.schedule.raportoi-epaonnistumiset-task")
    public Task<Void> raportoiEpaonnistumisetTask(Environment environment) {
        String scheduleString = environment.getRequiredProperty("varda-rekisterointi.schedule.raportoi-epaonnistumiset-task");
        return Tasks.recurring("raportoi-epaonnistumiset-task", parseSchedule(scheduleString))
                .execute((instance, ctx) -> taskMonitoringService.raportoiEpaonnistumiset());
    }

}
