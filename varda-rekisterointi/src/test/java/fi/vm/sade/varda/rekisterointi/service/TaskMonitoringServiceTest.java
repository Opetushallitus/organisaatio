package fi.vm.sade.varda.rekisterointi.service;

import fi.vm.sade.varda.rekisterointi.dao.ScheduledTaskDao;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.*;

public class TaskMonitoringServiceTest {

    @Test
    public void raportoiEpaonnistumisetEiLahetaViestia() {
        ScheduledTaskDao dao = mock(ScheduledTaskDao.class);
        when(dao.haeEpaonnistumiset()).thenReturn(Collections.emptySet());
        EmailService email = mock(EmailService.class);
        TaskMonitoringService service = new TaskMonitoringService(dao, email);
        service.raportoiEpaonnistumiset();
        verify(email, never()).lahetaOngelmaRaportti(anySet());
    }

    @Test
    public void raportoiEpaonnistumisetLahettaaViestin() {
        Set<TaskMonitoringService.TaskFailure> epaonnistumiset = Set.of(new TaskMonitoringService.TaskFailure(
                TaskMonitoringService.MonitoredTaskType.KUTSU_KAYTTAJA, 1L));
        ScheduledTaskDao dao = mock(ScheduledTaskDao.class);
        when(dao.haeEpaonnistumiset()).thenReturn(epaonnistumiset);
        EmailService email = mock(EmailService.class);
        TaskMonitoringService service = new TaskMonitoringService(dao, email);
        service.raportoiEpaonnistumiset();
        verify(email).lahetaOngelmaRaportti(anySet());
    }
}
