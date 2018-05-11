package fi.vm.sade.organisaatio.dao.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class LisatietoTyyppiDaoImpl extends AbstractJpaDAOImpl<Lisatietotyyppi, Long> implements LisatietoTyyppiDao {

    @Override
    public Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(String organisaatioOid) {
        QOrganisaatio organisaatio = QOrganisaatio.organisaatio;
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;
        QOrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = QOrganisaatiotyyppiRajoite.organisaatiotyyppiRajoite;
        QOppilaitostyyppiRajoite oppilaitostyyppiRajoite = QOppilaitostyyppiRajoite.oppilaitostyyppiRajoite;

        List<String> organisaatioLisatietotyyppiList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .distinct()
                .from(organisaatiotyyppiRajoite)
                .innerJoin(organisaatiotyyppiRajoite.lisatietotyyppi, lisatietotyyppi)
                .innerJoin(organisaatio).on(organisaatiotyyppiRajoite.arvo.in(organisaatio.tyypit))
                .where(organisaatio.oid.eq(organisaatioOid))
                .fetch();

        List<String> oppilaitosLisatietotyyppiList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .distinct()
                .from(oppilaitostyyppiRajoite)
                .innerJoin(oppilaitostyyppiRajoite.lisatietotyyppi, lisatietotyyppi)
                .innerJoin(organisaatio).on(oppilaitostyyppiRajoite.arvo.eq(organisaatio.oppilaitosTyyppi))
                .where(organisaatio.oid.eq(organisaatioOid))
                .fetch();

        List<String> notConstrainedTypesList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .from(lisatietotyyppi)
                .where(lisatietotyyppi.rajoitteet.isEmpty())
                .fetch();
        return Stream.of(oppilaitosLisatietotyyppiList, organisaatioLisatietotyyppiList, notConstrainedTypesList)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toSet());
    }
}
