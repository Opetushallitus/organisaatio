package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.organisaatio.RyhmaBuilder;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioDAOImpl;
import fi.vm.sade.organisaatio.dao.impl.OrganisaatioSuhdeDAOImpl;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.model.*;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {"classpath:spring/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class OrganisaatioDAOImplTest {

    Random r = new Random(0);

    private static final Logger LOG = LoggerFactory.getLogger(OrganisaatioDAOImplTest.class);

    @Autowired
    OrganisaatioDAOImpl organisaatioDAO;

    @Autowired
    OrganisaatioSuhdeDAOImpl organisaatioSuhdeDAO;

    @Test
    public void doTest() {
        LOG.info("doTest()...");
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", b, false, generateParentOids(b), generateParentIdPath(b));
        Organisaatio d = createOrganisaatio("D", c, false, generateParentOids(c), generateParentIdPath(c));
        Organisaatio e = createOrganisaatio("E", d, false, generateParentOids(d), generateParentIdPath(d));

        final String oid = e.getOid();
        List<String> oids = organisaatioDAO.findParentOidsTo(oid);
        Assert.assertEquals(5, oids.size());
        Assert.assertEquals(a.getOid(), oids.get(0));
        Assert.assertEquals(b.getOid(), oids.get(1));
        Assert.assertEquals(c.getOid(), oids.get(2));
        Assert.assertEquals(d.getOid(), oids.get(3));
        Assert.assertEquals(e.getOid(), oids.get(4));
    }

    @Test
    public void findGroupsTest() {
        Organisaatio parent1 = createOrganisaatio(generateOid(), "parent1", null, false, null, null);
        Organisaatio parent2 = createOrganisaatio(generateOid(), "parent2", null, false, null, null);
        Organisaatio ryhma1 = organisaatioDAO.insert(new RyhmaBuilder(generateOid())
                .parent(parent1)
                .nimi("FI", "ryhma1")
                .ryhmatyyppi("ryhmatyyppi1", "ryhmatyyppi2")
                .kayttoryhma("kayttoryhma1", "kayttoryhma2")
                .build());
        Organisaatio ryhma2 = organisaatioDAO.insert(new RyhmaBuilder(generateOid())
                .parent(parent1)
                .nimi("FI", "ryhma2-poistettu")
                .ryhmatyyppi("ryhmatyyppi2", "ryhmatyyppi3")
                .kayttoryhma("kayttoryhma2", "kayttoryhma3")
                .poistettu()
                .build());
        Organisaatio ryhma3 = organisaatioDAO.insert(new RyhmaBuilder(generateOid())
                .parent(parent2)
                .nimi("FI", "ryhma3-lakkautettu")
                .ryhmatyyppi("ryhmatyyppi1", "ryhmatyyppi3")
                .kayttoryhma("kayttoryhma1", "kayttoryhma3")
                .lakkautusPvm(LocalDate.of(2018, 5, 31))
                .build());

        RyhmaCriteriaDto criteria = new RyhmaCriteriaDto();
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("ma1");
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("olematon");
        assertThat(organisaatioDAO.findGroups(criteria)).isEmpty();

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 5, 30));
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(false);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setRyhmatyyppi("ryhmatyyppi1");
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setRyhmatyyppi("ryhmatyyppi2");
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setKayttoryhma("kayttoryhma1");
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setKayttoryhma("kayttoryhma2");
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setParentOid(parent2.getOid());
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setPoistettu(true);
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setPoistettu(false);
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("ryhma");
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        criteria.setRyhmatyyppi("ryhmatyyppi1");
        criteria.setKayttoryhma("kayttoryhma1");
        criteria.setParentOid(parent1.getOid());
        criteria.setPoistettu(false);
        assertThat(organisaatioDAO.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1);
    }

    @Test
    public void parentTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));

        b = organisaatioDAO.findByOid(b.getOid());

        Assert.assertEquals(a.getOid(), b.getParent().getOid());
    }

    @Test
    public void findChildrenTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio d = createOrganisaatio("D", b, false, generateParentOids(b), generateParentIdPath(b));
        Organisaatio e = createOrganisaatio("E", c, false, generateParentOids(c), generateParentIdPath(c));
        Organisaatio f = createOrganisaatio("F", a, true, generateParentOids(a), generateParentIdPath(a));

        List<Organisaatio> children = this.organisaatioDAO.findChildren(a.getOid(), false, true);
        Assert.assertEquals(2, children.size());
        Assert.assertTrue(organisaatiotContain(children, b));
        Assert.assertTrue(organisaatiotContain(children, c));
        Assert.assertFalse(organisaatiotContain(children, f));

        children = organisaatioDAO.findChildren(c.getOid(), false, true);
        Assert.assertEquals(1,children.size());
        Assert.assertTrue(organisaatiotContain(children, e));

        children = organisaatioDAO.findChildren(e.getOid(), false, true);
        Assert.assertEquals(0, children.size());

        children = organisaatioDAO.findChildren(d.getOid(), false, true);
        Assert.assertEquals(0, children.size());

        children = this.organisaatioDAO.findChildren(a.getOid(), true, true);
        Assert.assertEquals(3, children.size());
        Assert.assertTrue(organisaatiotContain(children, f));

    }

    @Test
    public void findModifedSinceLimitsByModificationTime() {
        Date now = new Date();
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(new Date(0L));
        organisaatioDAO.update(a);
        Organisaatio b = createOrganisaatio("B", null, false, null, null);
        b.setPaivitysPvm((new Date(now.getTime() + 100)));
        organisaatioDAO.update(b);
        List<Organisaatio> tulokset = organisaatioDAO.findModifiedSince(false, now);
        assertThat(tulokset.size()).isEqualTo(1);
        assertThat(tulokset.get(0).getOid()).isEqualTo(b.getOid());
    }

    @Test
    public void findModifedSinceLimitsByOrganizationType() {
        Date now = new Date();
        Date after = new Date(now.getTime() + 100);
        OrganisaatioTyyppi organisaatioTyyppi = OrganisaatioTyyppi.OPPILAITOS;
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(after);
        a.setTyypit(Collections.singleton(organisaatioTyyppi.koodiValue()));
        organisaatioDAO.update(a);
        Organisaatio b = createOrganisaatio("B", null, false, null, null);
        b.setPaivitysPvm(after);
        organisaatioDAO.update(b);
        List<Organisaatio> tulokset = organisaatioDAO.findModifiedSince(
                false, now, Collections.singletonList(organisaatioTyyppi), false);
        assertThat(tulokset.size()).isEqualTo(1);
        assertThat(tulokset.get(0).getOid()).isEqualTo(a.getOid());
    }

    @Test
    public void findModifedSinceExcludesDiscontinued() {
        Date now = new Date();
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(new Date(now.getTime() + 100));
        a.setLakkautusPvm(new Date(now.getTime() - 100));
        organisaatioDAO.update(a);
        List<Organisaatio> tulokset = organisaatioDAO.findModifiedSince(
                false, now, Collections.emptyList(), true);
        assertThat(tulokset.size()).isEqualTo(0);
    }

    @Test
    public void findModifedSinceIncludesDiscontinuedInTheFuture() {
        Date now = new Date();
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(new Date(now.getTime() + 100));
        a.setLakkautusPvm(new Date(now.getTime() + 24 * 60 * 60 * 1000));
        organisaatioDAO.update(a);
        List<Organisaatio> tulokset = organisaatioDAO.findModifiedSince(
                false, now, Collections.emptyList(), true);
        assertThat(tulokset.size()).isEqualTo(1);
    }

    private List<String> generateParentOids(Organisaatio parent) {
        if (parent == null) {
            return Collections.emptyList();
        }
        return Stream.concat(Stream.of(parent.getOid()), parent.getParentOids().stream()).collect(Collectors.toList());
    }

    private String generateParentIdPath(Organisaatio parent) {
        if (parent == null) {
            return null;
        }
        return (!StringUtils.isEmpty(parent.getParentIdPath()))
                ? parent.getParentIdPath() + parent.getId() + "|"
                : "|" + parent.getId() + "|";
    }

    private String generateOid() {
        return Long.toString(r.nextLong());
    }

    private boolean organisaatiotContain(List<Organisaatio> organisaatiot, Organisaatio o) {
        for (Organisaatio curOrg : organisaatiot) {
            if (curOrg.getId().equals(o.getId())) {
                return true;
            }
        }
        return false;
    }

    private Organisaatio createOrganisaatio(String nimi, Organisaatio parent, boolean isPoistettu, List<String> parentOids, String parentIdPath) {
        return createOrganisaatio(generateOid(), nimi, parent, isPoistettu, parentOids, parentIdPath);
    }

    private Organisaatio createOrganisaatio(String oid, String nimi, Organisaatio parent, boolean isPoistettu, List<String> parentOids, String parentIdPath) {
        LOG.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();


        o.setOid(oid);
        o.setOrganisaatioPoistettu(isPoistettu);

        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("FI", nimi);

        Set<Yhteystieto> oYhteystiedot = new HashSet<>();
        oYhteystiedot.add(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        o = organisaatioDAO.insert(o);

        if (parent != null) {
            OrganisaatioSuhde suhde = organisaatioSuhdeDAO.addChild(parent.getId(), o.getId(), null, null);
            o.getParentSuhteet().add(suhde);
            organisaatioSuhdeDAO.getEntityManager().flush();
            LOG.info("YHTEYSTIEDOT: " + o.getYhteystiedot().size());
            LOG.info("PARENTS: " + o.getParentSuhteet().size());
            o.setParentOids(parentOids);
            o.setParentIdPath(parentIdPath);
        }

        return o;
    }

    private Osoite createOsoite() {
        return new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
    }

}
