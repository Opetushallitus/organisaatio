package fi.vm.sade.organisaatio.repository.impl;

import fi.vm.sade.organisaatio.model.*;
import fi.vm.sade.organisaatio.repository.LisatietoTyyppiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class LisatietoTyyppiDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
    @Autowired
    private LisatietoTyyppiRepository lisatietoTyyppiRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        this.createAndPersistOrganisation("oid", "Oppilaitos", "oppilaitoskoodi_12#1");
    }

    private void createAndPersistOrganisation(String oid, String organisaatiotyyppi, String oppilaitostyyppi) {
        Organisaatio organisaatio = new Organisaatio();
        organisaatio.setNimi(new MonikielinenTeksti());
        organisaatio.setOid(oid);
        organisaatio.setTyypit(Collections.singleton(organisaatiotyyppi));
        organisaatio.setOppilaitosTyyppi(oppilaitostyyppi);
        this.entityManager.persist(organisaatio);
    }

    @Test
    public void findAll() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);

        Lisatietotyyppi lisatietotyyppi2 = new Lisatietotyyppi();
        lisatietotyyppi2.setNimi("lisatieto.withOrganisaatioRajoite");
        OrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = new OrganisaatiotyyppiRajoite();
        organisaatiotyyppiRajoite.setArvo("Oppilaitos");
        organisaatiotyyppiRajoite.setLisatietotyyppi(lisatietotyyppi2);
        lisatietotyyppi2.setRajoitteet(Collections.singleton(organisaatiotyyppiRajoite));
        // this.lisatietoTyyppiRepository.getEntityManager().persist(organisaatiotyyppiRajoite); //TODO check id rajoite is actually saved
        this.lisatietoTyyppiRepository.save(lisatietotyyppi2);

        Lisatietotyyppi lisatietotyyppi3 = new Lisatietotyyppi();
        lisatietotyyppi3.setNimi("lisatieto.withOppilaitosRajoite");
        OppilaitostyyppiRajoite oppilaitostyyppiRajoite = new OppilaitostyyppiRajoite();
        oppilaitostyyppiRajoite.setArvo("Oppilaitos");
        oppilaitostyyppiRajoite.setLisatietotyyppi(lisatietotyyppi3);
        lisatietotyyppi3.setRajoitteet(Collections.singleton(oppilaitostyyppiRajoite));
        //this.lisatietoTyyppiRepository.getEntityManager().persist(oppilaitostyyppiRajoite); //TODO check id rajoite is actually saved
        this.lisatietoTyyppiRepository.save(lisatietotyyppi3);

        List<Lisatietotyyppi> lisatietotyyppiList = StreamSupport.stream(this.lisatietoTyyppiRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
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
        // this.lisatietoTyyppiRepository.getEntityManager().persist(rajoite);
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.nimi");
    }

    @Test
    public void findByOrganisationWhenRajoitteetonLisatietotyyppi() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.rajoitteeton");
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.rajoitteeton");
    }

    @Test
    public void findByOrganisationWithOppilaitostyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OppilaitostyyppiRajoite rajoite = new OppilaitostyyppiRajoite();
        rajoite.setArvo("oppilaitoskoodi_12");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        // this.lisatietoTyyppiRepository.getEntityManager().persist(rajoite);
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).containsExactly("lisatieto.nimi");
    }

    @Test
    public void oppilaitostyyppiRajoiteIsNotFoundIncorrectly() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OppilaitostyyppiRajoite rajoite = new OppilaitostyyppiRajoite();
        rajoite.setArvo("oppilaitoskoodi_1");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        // this.lisatietoTyyppiRepository.getEntityManager().persist(rajoite);
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid");
        assertThat(lisatietotyyppiNimiList).isEmpty();
    }

    @Test
    public void findByOrganisationNotExistsWithOrganisaatiotyyppiRajoite() {
        Lisatietotyyppi lisatietotyyppi = new Lisatietotyyppi();
        lisatietotyyppi.setNimi("lisatieto.nimi");
        OrganisaatiotyyppiRajoite rajoite = new OrganisaatiotyyppiRajoite();
        rajoite.setArvo("Oppilaitos");
        rajoite.setLisatietotyyppi(lisatietotyyppi);
        lisatietotyyppi.setRajoitteet(Collections.singleton(rajoite));
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);
        //this.lisatietoTyyppiRepository.getEntityManager().persist(rajoite);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
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
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);
        //this.lisatietoTyyppiRepository.getEntityManager().persist(rajoiteOppilaitostyyppi);
        //this.lisatietoTyyppiRepository.getEntityManager().persist(rajoiteOrganisaatiotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
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
        this.lisatietoTyyppiRepository.save(lisatietotyyppi);
        //this.lisatietoTyyppiRepository.getEntityManager().persist(rajoiteOppilaitostyyppi);
        //this.lisatietoTyyppiRepository.getEntityManager().persist(rajoiteOrganisaatiotyyppi);

        Set<String> lisatietotyyppiNimiList = this.lisatietoTyyppiRepository
                .findValidByOrganisaatiotyyppiAndOppilaitostyyppi("oid2");
        assertThat(lisatietotyyppiNimiList)
                .containsExactly("lisatieto.nimi");
    }

}
