package fi.vm.sade.organisaatio.dao.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.QLisatietotyyppi;
import fi.vm.sade.organisaatio.model.QOppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.QOrganisaatiotyyppiRajoite;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.querydsl.core.types.ExpressionUtils.anyOf;


@Repository
public class LisatietoTyyppiDaoImpl extends AbstractJpaDAOImpl<Lisatietotyyppi, Long> implements LisatietoTyyppiDao {

    @Override
    public Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(List<String> organisaatiotyyppis,
                                                                        String oppilaitostyyppi) {
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;
        QOrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = QOrganisaatiotyyppiRajoite.organisaatiotyyppiRajoite;
        QOppilaitostyyppiRajoite oppilaitostyyppiRajoite = QOppilaitostyyppiRajoite.oppilaitostyyppiRajoite;

        List<String> organisaatioLisatietotyyppiList = new JPAQuery<>(this.getEntityManager())
                .from(organisaatiotyyppiRajoite)
                .innerJoin(organisaatiotyyppiRajoite.lisatietotyyppi, lisatietotyyppi)
                .where(anyOf(
                        organisaatiotyyppiRajoite.isNull(),
                        organisaatiotyyppiRajoite.arvo.in(organisaatiotyyppis)
                        )
                )
                .select(lisatietotyyppi.nimi).fetch();
        List<String> oppilaitosLisatietotyyppiList = new JPAQuery<>(this.getEntityManager())
                .from(oppilaitostyyppiRajoite)
                .innerJoin(oppilaitostyyppiRajoite.lisatietotyyppi, lisatietotyyppi)
                .where(anyOf(
                        oppilaitostyyppiRajoite.isNull(),
                        oppilaitostyyppiRajoite.arvo.eq(oppilaitostyyppi)
                        )
                )
                .select(lisatietotyyppi.nimi).fetch();
        return Stream.concat(oppilaitosLisatietotyyppiList.stream(), organisaatioLisatietotyyppiList.stream())
                .collect(Collectors.toSet());
    }
}
