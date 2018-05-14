package fi.vm.sade.organisaatio.dao.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.*;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class LisatietoTyyppiDaoImpl extends AbstractJpaDAOImpl<Lisatietotyyppi, Long> implements LisatietoTyyppiDao {

    @Override
    public Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(String organisaatioOid) {
        QOrganisaatio organisaatio = QOrganisaatio.organisaatio;
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;
        QOrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = QOrganisaatiotyyppiRajoite.organisaatiotyyppiRajoite;
        QOppilaitostyyppiRajoite oppilaitostyyppiRajoite = QOppilaitostyyppiRajoite.oppilaitostyyppiRajoite;

        List<String> lisatietotyyppiNimiList = new JPAQuery<>(this.getEntityManager())
                .select(lisatietotyyppi.nimi)
                .distinct()
                .from(lisatietotyyppi)
                .innerJoin(organisaatio).on(organisaatio.oid.eq(organisaatioOid))
                .innerJoin(organisaatiotyyppiRajoite).on(organisaatiotyyppiRajoite.lisatietotyyppi.eq(lisatietotyyppi))
                .innerJoin(oppilaitostyyppiRajoite).on(oppilaitostyyppiRajoite.lisatietotyyppi.eq(lisatietotyyppi))
                .where(organisaatiotyyppiRajoite.arvo.in(organisaatio.tyypit)
                        .or(oppilaitostyyppiRajoite.arvo.eq(organisaatio.oppilaitosTyyppi))
                        .or(lisatietotyyppi.rajoitteet.isEmpty()))
                .fetch();

        return new HashSet<>(lisatietotyyppiNimiList);
    }

    @Override
    public Optional<Lisatietotyyppi> findByNimi(String nimi) {
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;

        Lisatietotyyppi lisatietotyyppiResult = new JPAQuery<Lisatietotyyppi>(this.getEntityManager())
                .select(lisatietotyyppi)
                .from(lisatietotyyppi)
                .where(lisatietotyyppi.nimi.eq(nimi))
                .fetchFirst();
        return Optional.ofNullable(lisatietotyyppiResult);
    }
}
