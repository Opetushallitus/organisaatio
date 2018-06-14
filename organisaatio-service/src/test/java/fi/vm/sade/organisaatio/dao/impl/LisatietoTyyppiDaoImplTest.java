package fi.vm.sade.organisaatio.dao.impl;

import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.OppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.Organisaatio;
import fi.vm.sade.organisaatio.model.OrganisaatiotyyppiRajoite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(locations = {
        "classpath:spring/test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class LisatietoTyyppiDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    private LisatietoTyyppiDaoImpl lisatietoTyyppiDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void setup() {
        this.createAndPersistOrganisation("oid", "Oppilaitos", "oppilaitoskoodi_12#1");
    }

    private void createAndPersistOrganisation(String oid, String organisaatiotyyppi, String oppilaitostyyppi) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setOid(oid);
        organisaatio.setTyypit(Collections.singletonList(organisaatiotyyppi));
        organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
        this.entityManager.persist(organisaatio);
    }

    @Test
    public void findAll() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        this.lisatietoTyyppiDao.insert(lisatietotyyppi);

        Lisatietotyyppi lisatietotyyppi2 = new Lisatietotyyppi();
        lisatietotyyppi2.setNimi("lisatieto.withOrganisaatioRajoite");
        OrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = new OrganisaatiotyyppiRajoite();
        organisaatiotyyppiRajoite.setArvo("Oppilaitos");
        organisaatiotyyppiRajoite.setLisatietotyyppi(lisatietotyyppi2);
        lisatietotyyppi2.setRajoitteet(Collections.singleton(organisaatiotyyppiRajoite));
        this.lisatietoTyyppiDao.getEntityManager().persist(organisaatiotyyppiRajoite);
        this.lisatietoTyyppiDao.insert(lisatietotyyppi2);

        Lisatietotyyppi lisatietotyyppi3 = new Lisatietotyyppi();
        lisatietotyyppi3.setNimi("lisatieto.withOppilaitosRajoite");
        OppilaitostyyppiRajoite oppilaitostyyppiRajoite = new OppilaitostyyppiRajoite();
        oppilaitostyyppiRajoite.setArvo("Oppilaitos");
        oppilaitostyyppiRajoite.setLisatietotyyppi(lisatietotyyppi3);
        lisatietotyyppi3.setRajoitteet(Collections.singleton(oppilaitostyyppiRajoite));
        this.lisatietoTyyppiDao.getEntityManager().persist(oppilaitostyyppiRajoite);
        this.lisatietoTyyppiDao.insert(lisatietotyyppi3);

        List<Lisatietotyyppi> lisatietotyyppiList = this.lisatietoTyyppiDao.findAll();
        assertThat(lisatietotyyppiList)
                .extracting(Lisatietotyyppi::getNimi)
                .containsExactlyInAnyOrder("lisatieto.nimi", "lisatieto.withOrganisaatioRajoite", "lisatieto.withOppilaitosRajoite");
    }

    @Test
    public void findByOrganisationWithOrganisaatiotyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OrganisaatiotyyppiRajoite rajoite = new OrganisaatiotyyppiRajoite();
        rajoite.setArvo("Oppilaitos");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoite);
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.nimi");
    }

    @Test
    public void findByOrganisationWhenRajoitteetonLisatietotyyppi() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.rajoitteeton");
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.rajoitteeton");
    }

    @Test
    public void findByOrganisationWithOppilaitostyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OppilaitostyyppiRajoite rajoite = new OppilaitostyyppiRajoite();
        rajoite.setArvo("oppilaitoskoodi_12#1");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoite);
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.nimi");
    }

    @Test
    public void findByOrganisationNotExistsWithOrganisaatiotyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OrganisaatiotyyppiRajoite rajoite = new OrganisaatiotyyppiRajoite();
        rajoite.setArvo("Oppilaitos");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoite);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("wrong oid");
        assertThat(lisatietotyyppiNimiList).isEmpty();
    }

    @Test
    public void findByOrganisationWithOrganisaatiotyyppiAndOppilaitostyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");

        OrganisaatiotyyppiRajoite rajoiteOrganisaatiotyyppi = new OrganisaatiotyyppiRajoite();
        rajoiteOrganisaatiotyyppi.setArvo("Oppilaitos");
        rajoiteOrganisaatiotyyppi.setLisatietotyyppi(lisatietotyyppi);

        OppilaitostyyppiRajoite rajoiteOppilaitostyyppi = new OppilaitostyyppiRajoite();
        rajoiteOppilaitostyyppi.setArvo("oppilaitoskoodi_12#1");
        rajoiteOppilaitostyyppi.setLisatietotyyppi(lisatietotyyppi);

        lisatietotyyppi.setRajoitteet(Stream.of(rajoiteOppilaitostyyppi, rajoiteOrganisaatiotyyppi).collect(Collectors.toSet()));
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoiteOppilaitostyyppi);
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoiteOrganisaatiotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList)
                .containsExactly("lisatieto.nimi");
    }

    @Test
    public void findByOrganisationWithOrganisaatiotyyppiAndOppilaitostyyppiRajoiteWhileOnlyOneConstraintIsTrue() {
        this.createAndPersistOrganisation("oid2", "Koulutustoimija", null);

        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");

        OrganisaatiotyyppiRajoite rajoiteOrganisaatiotyyppi = new OrganisaatiotyyppiRajoite();
        rajoiteOrganisaatiotyyppi.setArvo("Koulutustoimija");
        rajoiteOrganisaatiotyyppi.setLisatietotyyppi(lisatietotyyppi);

        OppilaitostyyppiRajoite rajoiteOppilaitostyyppi = new OppilaitostyyppiRajoite();
        rajoiteOppilaitostyyppi.setArvo("oppilaitoskoodi_12#1");
        rajoiteOppilaitostyyppi.setLisatietotyyppi(lisatietotyyppi);

        lisatietotyyppi.setRajoitteet(Stream.of(rajoiteOppilaitostyyppi, rajoiteOrganisaatiotyyppi).collect(Collectors.toSet()));
        this.lisatietoTyyppiDao.getEntityManager().persist(lisatietotyyppi);
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoiteOppilaitostyyppi);
        this.lisatietoTyyppiDao.getEntityManager().persist(rajoiteOrganisaatiotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiDao
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid2");
        assertThat(lisatietotyyppiNimiList)
                .containsExactly("lisatieto.nimi");
    }

}
