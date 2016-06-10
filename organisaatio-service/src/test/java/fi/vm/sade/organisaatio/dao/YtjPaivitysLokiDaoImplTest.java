package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.organisaatio.dao.impl.YtjPaivitysLokiDaoImpl;
import fi.vm.sade.organisaatio.model.YtjPaivitysLoki;
import fi.vm.sade.organisaatio.model.YtjVirhe;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class YtjPaivitysLokiDaoImplTest {

    @Autowired
    private YtjPaivitysLokiDaoImpl ytjPaivitysLokiDao;

    YtjPaivitysLoki oldLog = new YtjPaivitysLoki();
    YtjPaivitysLoki newLog = new YtjPaivitysLoki();

    @Before
    public void setUp() {
        YtjVirhe virhe = new YtjVirhe();
        virhe.setOid("12345.0");
        virhe.setVirhekentta("foo");
        virhe.setVirheviesti("bar");
        // vanhempi loki
        oldLog.setPaivitetytLkm(1);
        oldLog.setPaivitysaika(createDate(2017, 0, 1));
        oldLog.setYtjVirheet((new ArrayList<YtjVirhe>()));
        oldLog.getYtjVirheet().add(virhe);
        ytjPaivitysLokiDao.insert(oldLog);
        // uudempi loki
        newLog.setPaivitetytLkm(1);
        newLog.setPaivitysaika(createDate(2017, 5 ,5));
        newLog.setYtjVirheet((new ArrayList<YtjVirhe>()));
        newLog.getYtjVirheet().add(virhe);
        ytjPaivitysLokiDao.insert(newLog);
    }

    @After
    public void tearDown() {

    }

    public Date createDate(int year, int month, int day) {
        return new GregorianCalendar(year, month, day).getTime();
    }

    @Test
    public void fetchOnlyNewLog() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiDao.findByDateRange(createDate(2017, 2, 2), createDate(2017, 6, 6));
        Assert.assertEquals(1, logs.size());
        Assert.assertFalse(logs.get(0).getYtjVirheet().isEmpty());
        Assert.assertEquals(1, logs.get(0).getYtjVirheet().size());
    }

    @Test
    @Ignore
    public void fetchBothLogs() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiDao.findByDateRange(createDate(2016, 2, 2), createDate(2018, 6, 6));
        // TODO add data cleanup so that tests don't mess each other's data (OrgYtjServiceImplTest)
        Assert.assertEquals(2, logs.size());
    }

    @Test
    public void fetchLatest() {
        List<YtjPaivitysLoki> logs = ytjPaivitysLokiDao.findLatest(1);
        Assert.assertEquals(1, logs.size());
        Assert.assertEquals(newLog.getId(), logs.get(0).getId());
    }
}
