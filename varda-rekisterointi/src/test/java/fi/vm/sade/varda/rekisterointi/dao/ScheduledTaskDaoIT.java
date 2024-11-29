package fi.vm.sade.varda.rekisterointi.dao;

import fi.vm.sade.varda.rekisterointi.service.TaskMonitoringService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"integration-test", "dev"})
@Transactional
@AutoConfigureTestDatabase
public class ScheduledTaskDaoIT {

    @Autowired
    private ScheduledTaskDao dao;

    @Test
    public void eiEpaonnistuneitaScheduledTaskeja() {
        Set<TaskMonitoringService.TaskFailure> epaonnistumiset = dao.haeEpaonnistumiset();
        assertTrue(epaonnistumiset.isEmpty());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO scheduled_tasks " +
                    "(task_name, task_instance, execution_time, picked, version, consecutive_failures) VALUES " +
                    "('luo-tai-paivita-organisaatio-task', 'luo-tai-paivita-organisaatio-task-1', CURRENT_TIMESTAMP, true, 0, 4)," +
                    "('kutsu-kayttaja-task', 'kutsu-kayttaja-task-2', CURRENT_TIMESTAMP, true, 0, 4);"
    })
    public void epaonnistuneitaScheduledTaskeja() {
        Set<TaskMonitoringService.TaskFailure> epaonnistumiset = dao.haeEpaonnistumiset();
        assertEquals(2, epaonnistumiset.size());
        assertTrue(epaonnistumiset.contains(new TaskMonitoringService.TaskFailure(
                TaskMonitoringService.MonitoredTaskType.LUO_TAI_PAIVITA_ORGANISAATIO, 1L
        )));
        assertTrue(epaonnistumiset.contains(new TaskMonitoringService.TaskFailure(
                TaskMonitoringService.MonitoredTaskType.KUTSU_KAYTTAJA, 2L
        )));
    }
}
