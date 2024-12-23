package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.dao.ScheduledTaskDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TaskMonitoringService {

    public static final int MAX_ALLOWED_FAILURES = 3;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMonitoringService.class);
    private final ScheduledTaskDao scheduledTaskDao;
    private final EmailService emailService;

    public TaskMonitoringService(ScheduledTaskDao scheduledTaskDao, EmailService emailService) {
        this.scheduledTaskDao = scheduledTaskDao;
        this.emailService = emailService;
    }

    public void raportoiEpaonnistumiset() {
        Set<TaskFailure> epaonnistumiset = scheduledTaskDao.haeEpaonnistumiset();
        LOGGER.info("Yli {} kertaa epäonnistuneita taskeja löytyi: {}", MAX_ALLOWED_FAILURES, epaonnistumiset.size());
        if (!epaonnistumiset.isEmpty()) {
            emailService.lahetaOngelmaRaportti(epaonnistumiset);
        }
    }

    public enum MonitoredTaskType {
        // viestintäpalvelun feilaamisesta kyllä nousee haloo, ei vahdita emaileja!
        LUO_TAI_PAIVITA_ORGANISAATIO("luo-tai-paivita-organisaatio-task"),
        KUTSU_KAYTTAJA("kutsu-kayttaja-task");

        public final String taskName;

        MonitoredTaskType(String taskName) {
            this.taskName = taskName;
        }
    }

    public static class TaskFailure {
        public final MonitoredTaskType tyyppi;
        public final Long rekisterointi;
        public TaskFailure(MonitoredTaskType tyyppi, Long rekisterointi) {
            this.tyyppi = tyyppi;
            this.rekisterointi = rekisterointi;
        }
        @Override
        public boolean equals(Object other) {
            if (other instanceof TaskFailure) {
                TaskFailure tf = (TaskFailure) other;
                return tyyppi == tf.tyyppi && rekisterointi.equals(tf.rekisterointi);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return 97 * tyyppi.hashCode() + 11 * rekisterointi.hashCode();
        }
    }

}
