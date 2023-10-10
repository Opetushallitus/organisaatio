package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.RyhmaBuilder;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.dto.mapping.RyhmaCriteriaDto;
import fi.vm.sade.organisaatio.model.*;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@Sql("/data/truncate_tables.sql")
class OrganisaatioRepositoryImplTest {

    Random r = new Random(0);

    private static final Logger log = LoggerFactory.getLogger(OrganisaatioRepositoryImplTest.class);

    @Autowired
    OrganisaatioRepository organisaatioRepository;

    @Autowired
    OrganisaatioSuhdeRepository organisaatioSuhdeRepository;

    @Test
    void doTest() {
        log.info("doTest()...");
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", b, false, generateParentOids(b), generateParentIdPath(b));
        Organisaatio d = createOrganisaatio("D", c, false, generateParentOids(c), generateParentIdPath(c));
        Organisaatio e = createOrganisaatio("E", d, false, generateParentOids(d), generateParentIdPath(d));

        final String oid = e.getOid();
        List<String> oids = organisaatioRepository.findParentOidsTo(oid);
        assertEquals(5, oids.size());
        assertEquals(a.getOid(), oids.get(0));
        assertEquals(b.getOid(), oids.get(1));
        assertEquals(c.getOid(), oids.get(2));
        assertEquals(d.getOid(), oids.get(3));
        assertEquals(e.getOid(), oids.get(4));
    }

    @Test
    void findGroupsTest() {
        Organisaatio parent1 = createOrganisaatio(generateOid(), "parent1", null, false, null, null);
        Organisaatio parent2 = createOrganisaatio(generateOid(), "parent2", null, false, null, null);
        Organisaatio ryhma1 = organisaatioRepository.save(new RyhmaBuilder(generateOid())
                .parent(parent1)
                .nimi("FI", "ryhma1")
                .ryhmatyyppi("ryhmatyyppi1", "ryhmatyyppi2")
                .kayttoryhma("kayttoryhma1", "kayttoryhma2")
                .build());
        Organisaatio ryhma2 = organisaatioRepository.save(new RyhmaBuilder(generateOid())
                .parent(parent1)
                .nimi("FI", "ryhma2-poistettu")
                .ryhmatyyppi("ryhmatyyppi2", "ryhmatyyppi3")
                .kayttoryhma("kayttoryhma2", "kayttoryhma3")
                .poistettu()
                .build());
        Organisaatio ryhma3 = organisaatioRepository.save(new RyhmaBuilder(generateOid())
                .parent(parent2)
                .nimi("FI", "ryhma3-lakkautettu")
                .ryhmatyyppi("ryhmatyyppi1", "ryhmatyyppi3")
                .kayttoryhma("kayttoryhma1", "kayttoryhma3")
                .lakkautusPvm(LocalDate.of(2018, 5, 31))
                .build());

        RyhmaCriteriaDto criteria = new RyhmaCriteriaDto();
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("ma1");
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("olematon");
        assertThat(organisaatioRepository.findGroups(criteria)).isEmpty();

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 5, 30));
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setAktiivinen(false);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setRyhmatyyppi("ryhmatyyppi1");
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setRyhmatyyppi("ryhmatyyppi2");
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setKayttoryhma("kayttoryhma1");
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setKayttoryhma("kayttoryhma2");
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setParentOid(parent2.getOid());
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setPoistettu(true);
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma2);

        criteria = new RyhmaCriteriaDto();
        criteria.setPoistettu(false);
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1, ryhma3);

        criteria = new RyhmaCriteriaDto();
        criteria.setQ("ryhma");
        criteria.setAktiivinen(true);
        criteria.setLakkautusPvm(LocalDate.of(2018, 6, 1));
        criteria.setRyhmatyyppi("ryhmatyyppi1");
        criteria.setKayttoryhma("kayttoryhma1");
        criteria.setParentOid(parent1.getOid());
        criteria.setPoistettu(false);
        assertThat(organisaatioRepository.findGroups(criteria)).containsExactlyInAnyOrder(ryhma1);
    }

    @Test
    void parentTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));

        b = organisaatioRepository.findFirstByOid(b.getOid());

        assertEquals(a.getOid(), b.getParent().getOid());
    }

    @Test
    void findChildrenTest() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        Organisaatio b = createOrganisaatio("B", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio c = createOrganisaatio("C", a, false, generateParentOids(a), generateParentIdPath(a));
        Organisaatio d = createOrganisaatio("D", b, false, generateParentOids(b), generateParentIdPath(b));
        Organisaatio e = createOrganisaatio("E", c, false, generateParentOids(c), generateParentIdPath(c));
        Organisaatio f = createOrganisaatio("F", a, true, generateParentOids(a), generateParentIdPath(a));

        List<Organisaatio> children = this.organisaatioRepository.findChildren(a.getOid(), false, true);
        assertEquals(2, children.size());
        assertTrue(organisaatiotContain(children, b));
        assertTrue(organisaatiotContain(children, c));
        assertFalse(organisaatiotContain(children, f));

        children = organisaatioRepository.findChildren(c.getOid(), false, true);
        assertEquals(1, children.size());
        assertTrue(organisaatiotContain(children, e));

        children = organisaatioRepository.findChildren(e.getOid(), false, true);
        assertEquals(0, children.size());

        children = organisaatioRepository.findChildren(d.getOid(), false, true);
        assertEquals(0, children.size());

        children = this.organisaatioRepository.findChildren(a.getOid(), true, true);
        assertEquals(3, children.size());
        assertTrue(organisaatiotContain(children, f));

    }

    @Test
    void findModifedSinceLimitsByModificationTime() {
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        organisaatioRepository.saveAndFlush(a);

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        LocalDateTime before = LocalDateTime.now();
        Organisaatio b = createOrganisaatio("B", null, false, null, null);
        organisaatioRepository.save(b);
        List<Organisaatio> tulokset = organisaatioRepository.findModifiedSince(false, before);
        assertThat(tulokset.size()).isEqualTo(1);
        assertThat(tulokset.get(0).getOid()).isEqualTo(b.getOid());
    }

    @Test
    void findModifedSinceLimitsByOrganizationType() {
        LocalDateTime now = LocalDateTime.now();
        Date after = java.sql.Date.valueOf(now.plusSeconds(1).toLocalDate());
        OrganisaatioTyyppi organisaatioTyyppi = OrganisaatioTyyppi.OPPILAITOS;
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(after);
        a.setTyypit(Collections.singleton(organisaatioTyyppi.koodiValue()));
        organisaatioRepository.save(a);
        Organisaatio b = createOrganisaatio("B", null, false, null, null);
        b.setPaivitysPvm(after);
        organisaatioRepository.save(b);
        List<Organisaatio> tulokset = organisaatioRepository.findModifiedSince(
                false, now, Collections.singletonList(organisaatioTyyppi), false);
        assertThat(tulokset.size()).isEqualTo(1);
        assertThat(tulokset.get(0).getOid()).isEqualTo(a.getOid());
    }

    @Test
    void findModifedSinceExcludesDiscontinued() {
        LocalDateTime now = LocalDateTime.now();
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(java.sql.Date.valueOf(now.plusSeconds(1).toLocalDate()));
        a.setLakkautusPvm(java.sql.Date.valueOf(now.minusSeconds(1).toLocalDate()));
        organisaatioRepository.save(a);
        List<Organisaatio> tulokset = organisaatioRepository.findModifiedSince(
                false, now, Collections.emptyList(), true);
        assertThat(tulokset.size()).isEqualTo(0);
    }

    @Test
    void findModifedSinceIncludesDiscontinuedInTheFuture() {
        LocalDateTime now = LocalDateTime.now();
        Organisaatio a = createOrganisaatio("A", null, false, null, null);
        a.setPaivitysPvm(java.sql.Date.valueOf(now.plusSeconds(1).toLocalDate()));
        a.setLakkautusPvm(java.sql.Date.valueOf(now.plusHours(24).toLocalDate()));
        organisaatioRepository.save(a);
        List<Organisaatio> tulokset = organisaatioRepository.findModifiedSince(
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
        log.info("createOrganisaatio({})", nimi);

        Organisaatio o = new Organisaatio();


        o.setOid(oid);
        o.setOrganisaatioPoistettu(isPoistettu);

        o.setNimi(new MonikielinenTeksti());
        o.getNimi().addString("FI", nimi);

        Set<Yhteystieto> oYhteystiedot = new HashSet<>();
        oYhteystiedot.add(createOsoite());
        o.setYhteystiedot(oYhteystiedot);

        o = organisaatioRepository.save(o);

        if (parent != null) {
            OrganisaatioSuhde childRelation = new OrganisaatioSuhde();
            childRelation.setAlkuPvm(new Date());
            childRelation.setLoppuPvm(null);
            childRelation.setChild(o);
            childRelation.setParent(parent);
            childRelation.setOpetuspisteenJarjNro(null);
            organisaatioSuhdeRepository.save(childRelation);

            o.getParentSuhteet().add(childRelation);
            log.info("YHTEYSTIEDOT: " + o.getYhteystiedot().size());
            log.info("PARENTS: " + o.getParentSuhteet().size());
            o.setParentOids(parentOids);
            o.setParentIdPath(parentIdPath);
        }

        return o;
    }

    private Osoite createOsoite() {
        return new Osoite(Osoite.TYYPPI_KAYNTIOSOITE, "katu", "0000", "Helsinki", UUID.randomUUID().toString());
    }
}
