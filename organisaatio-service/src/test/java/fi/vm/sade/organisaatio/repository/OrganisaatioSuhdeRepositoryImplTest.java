package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
public class OrganisaatioSuhdeRepositoryImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioSuhdeRepositoryImplTest.class);
    @AfterEach
    public void cleanup() {
        executeSqlScript("classpath:data/truncate_tables.sql", false);
    }
    @Autowired
    OrganisaatioSuhdeRepository organisaatioSuhdeRepository;

    @Autowired
    OrganisaatioRepository organisaatioRepository;

    @Test
    public void doTest() {
        LOG.info("doTest()...");
        int counter=12345;
        //Organisaatio that resembles the hidden root organisation (i.e. the parent of roots)
        Organisaatio OPH = createOrganisaatio("OPH",(String.valueOf(counter++).concat(".0")));

        Date t = createDate("1.1.2000");

        Organisaatio a = createOrganisaatio("A", (String.valueOf(counter++).concat(".0")));
        addChild(OPH, a, t);
        Organisaatio b = createOrganisaatio("B", (String.valueOf(counter++).concat(".0")));
        addChild(OPH, b, t);
        Organisaatio c = createOrganisaatio("C", (String.valueOf(counter++).concat(".0")));
        addChild(OPH, c, t);
        Organisaatio d = createOrganisaatio("D", (String.valueOf(counter++).concat(".0")));
        addChild(OPH, d, t);
        Organisaatio e = createOrganisaatio("E", (String.valueOf(counter++).concat(".0")));
        addChild(OPH, e, t);

        printOrganisaatioSuhdeTable();


        t = createDate("1.1.2001");
        addChild(a, b, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, a.getId(), b.getId()));

        t = createDate("2.1.2001");
        addChild(a, c, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));

        t = createDate("3.1.2001");
        addChild(d, e, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, d.getId(), e.getId()));

        t = createDate("4.1.2001");
        addChild(OPH, e, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, d.getId()));

        t = createDate("5.1.2001");
        addChild(c, b, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, c.getId(), b.getId()));

        t = createDate("6.1.2001");
        addChild(c, d, t);
        printOrganisaatioSuhdeTable();
        assertTrue(verifyChildren(t, c.getId(), b.getId(), d.getId()));

        //
        // Test "history"
        //

        // "before everything"
        t = createDate("1.1.2000");
        assertTrue(verifyChildren(t, a.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("1.1.2001");
        // after a -> b
        assertTrue(verifyChildren(t, a.getId(), b.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("2.1.2001");
        // after a -> b
        // after a -> c
        assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("3.1.2001");
        // after a -> b
        // after a -> c
        // after d -> e
        assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId()));
        assertTrue(verifyChildren(t, d.getId(), e.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("4.1.2001");
        // after a -> b
        // after a -> c
        // after d -> e  this removed
        assertTrue(verifyChildren(t, a.getId(), b.getId(), c.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("5.1.2001");
        // after a -> b  this removed
        // after a -> c
        // after d -> e  this removed
        // after c -> b
        assertTrue(verifyChildren(t, a.getId(), c.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId(), b.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        t = createDate("6.1.2001");
        // after a -> b  this removed
        // after a -> c
        // after d -> e  this removed
        // after c -> b
        // after c -> d
        assertTrue(verifyChildren(t, a.getId(), c.getId()));
        assertTrue(verifyChildren(t, b.getId()));
        assertTrue(verifyChildren(t, c.getId(), b.getId(), d.getId()));
        assertTrue(verifyChildren(t, d.getId()));
        assertTrue(verifyChildren(t, e.getId()));

        printOrganisaatioSuhdeTable();

        t = new Date();
        this.organisaatioSuhdeRepository.delete(this.organisaatioSuhdeRepository.findParentTo(e.getId(), new Date()));
        assertTrue(verifyChildren(t, d.getId(), e.getId()));


        //
        // Test organisation name history
        //

        LOG.info("doTest()... done.");
    }

    @Test
    public void findForDay() throws Exception {
        executeSqlScript("classpath:data/organisaatiosuhde_data.sql", false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date = dateFormat.parse("2000-01-01");
        LOG.info("Quering with date {}", date);
        List<OrganisaatioSuhde> list = organisaatioSuhdeRepository.findForDay(date);
        assertEquals(4, list.size());
    }

    @Test
    public void findForDayWithNull() throws Exception {
        executeSqlScript("classpath:data/organisaatiosuhde_data.sql", false);
        List<OrganisaatioSuhde> list = organisaatioSuhdeRepository.findForDay(null);
        assertTrue(list.isEmpty());
    }

    @Test
    public void findForDayWithZeroDate() throws Exception {
        executeSqlScript("classpath:data/organisaatiosuhde_data.sql", false);
        List<OrganisaatioSuhde> list = organisaatioSuhdeRepository.findForDay(new Date(0));
        assertTrue(list.isEmpty());
    }

    private void printOrganisaatioSuhdeTable() {
        Iterable<OrganisaatioSuhde> oss = organisaatioSuhdeRepository.findAll();

        LOG.info("ORGANISAATIOSUHDE TABLE:");
        for (OrganisaatioSuhde os : oss) {
            LOG.info("  OS: pId={}, cId={}, a={}, l={}",
                    new Object[]{os.getParent().getId(), os.getChild().getId(), os.getAlkuPvm(), os.getLoppuPvm()});
        }
    }

    private boolean verifyChildren(Date d, Long parentId, Long... children) {
        List<OrganisaatioSuhde> oss = organisaatioSuhdeRepository.findChildrenTo(parentId, d);

        if (oss.size() != children.length) {
            LOG.info("verifyChildren : expected {} != actual {} ???", children.length, oss.size());
            return false;
        }

        for (Long childId : children) {
            OrganisaatioSuhde os = organisaatioSuhdeRepository.findParentTo(childId, d);
            if (os == null || os.getParent().getId() != parentId) {
                LOG.info("Invalid parent! expected: {}, was {}", parentId, (os != null) ? os.getParent().getId() : "NULL");
                return false;
            }
        }

        return true;
    }

    private Date createDate(String s) {
        LOG.info("-------------------------------- createDate({})", s);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            return sdf.parse(s);
        } catch (ParseException e) {
            fail("Invalid date in test: " + s);
            return null;
        }
    }

    private Organisaatio createOrganisaatio(String nimi, String oid) {
        LOG.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();
        o.setOid(oid);
        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("FI", nimi);

        Set<Yhteystieto> oYhteystiedot = new HashSet<Yhteystieto>();
        oYhteystiedot.add(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        organisaatioRepository.save(o);

        return o;
    }

    private Osoite createOsoite() {
        Osoite o = new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
        return o;
    }

    private void addChild(Organisaatio parent, Organisaatio child, Date startingFrom){
        OrganisaatioSuhde childRelation = new OrganisaatioSuhde();
        childRelation.setAlkuPvm(startingFrom);
        childRelation.setLoppuPvm(null);
        childRelation.setChild(child);
        childRelation.setParent(parent);
        childRelation.setOpetuspisteenJarjNro(null);
        childRelation = organisaatioSuhdeRepository.save(childRelation);
    }
}
