package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
public class YtjPaivitysLokiRepositoryImplTest {

    @Autowired
    private YtjPaivitysLokiRepository ytjPaivitysLokiRepository;

    YtjPaivitysLoki oldLog = new YtjPaivitysLoki();
    YtjPaivitysLoki newLog = new YtjPaivitysLoki();

    @BeforeEach
    public void setUp() {
        YtjVirhe virhe = new YtjVirhe();
        virhe.setOid("12345.0");
        virhe.setVirhekohde(YtjVirhe.YTJVirheKohde.ALKUPVM);
        virhe.setVirheviesti("bar");
        // vanhempi loki
        oldLog.setPaivitetytLkm(1);
        oldLog.setPaivitysaika(createDate(2017, 0, 1));
        oldLog.setYtjVirheet((new ArrayList<YtjVirhe>()));
        oldLog.getYtjVirheet().add(virhe);
        ytjPaivitysLokiRepository.save(oldLog);
        // uudempi loki
        newLog.setPaivitetytLkm(1);
        newLog.setPaivitysaika(createDate(2017, 5 ,5));
        newLog.setYtjVirheet((new ArrayList<YtjVirhe>()));
        newLog.getYtjVirheet().add(virhe);
        ytjPaivitysLokiRepository.save(newLog);
    }

    public Date createDate(int year, int month, int day) {
        return new GregorianCalendar(year, month, day).getTime();
    }

    @Test
    public void fetchOnlyNewLog() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiRepository.findByDateRange(createDate(2017, 2, 2), createDate(2017, 6, 6));
        assertEquals(1, logs.size());
        assertFalse(logs.get(0).getYtjVirheet().isEmpty());
        assertEquals(1, logs.get(0).getYtjVirheet().size());
    }

    @Test
    public void fetchBothLogs() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiRepository.findByDateRange(createDate(2016, 2, 2), createDate(2018, 6, 6));
        assertEquals(2, logs.size());
    }

    @Test
    public void fetchLatest() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiRepository.findLatest(1);
        assertEquals(1, logs.size());
        assertEquals(newLog.getId(), logs.get(0).getId());
    }
}
