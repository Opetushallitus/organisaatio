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

        List<String> rajoiteLisatietotyyppiNimiList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .distinct()
                .from(lisatietotyyppi)
                .innerJoin(organisaatio).on(organisaatio.oid.eq(organisaatioOid))
                .innerJoin(organisaatiotyyppiRajoite).on(organisaatiotyyppiRajoite.lisatietotyyppi.eq(lisatietotyyppi))
                .innerJoin(oppilaitostyyppiRajoite).on(oppilaitostyyppiRajoite.lisatietotyyppi.eq(lisatietotyyppi))
                .where(organisaatiotyyppiRajoite.arvo.in(organisaatio.tyypit)
                        .or(oppilaitostyyppiRajoite.arvo.eq(organisaatio.oppilaitosTyyppi)))
                .fetch();

        List<String> eiRajoiteLisatietoTyyppiNimiList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .from(lisatietotyyppi)
                .where(lisatietotyyppi.rajoitteet.isEmpty())
                .fetch();
        return Stream.of(rajoiteLisatietotyyppiNimiList, eiRajoiteLisatietoTyyppiNimiList)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toSet());
    }
}
