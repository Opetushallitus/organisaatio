package fi.vm.sade.organisaatio.dao.impl;

import com.querydsl.jpa.impl.JPAQuery;
import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.QLisatietotyyppi;
import fi.vm.sade.organisaatio.model.QOppilaitostyyppiRajoite;
import fi.vm.sade.organisaatio.model.QOrganisaatiotyyppiRajoite;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.querydsl.core.types.ExpressionUtils.anyOf;


@Repository
public class LisatietoTyyppiDaoImpl extends AbstractJpaDAOImpl<Lisatietotyyppi, Long> implements LisatietoTyyppiDao {

    @Override
    public Set<String> findValidByOrganisaatiotyyppiAndOppilaitostyyppi(List<String> organisaatiotyyppis,
                                                                        String oppilaitostyyppi) {
        QLisatietotyyppi lisatietotyyppi = QLisatietotyyppi.lisatietotyyppi;
        QOrganisaatiotyyppiRajoite organisaatiotyyppiRajoite = QOrganisaatiotyyppiRajoite.organisaatiotyyppiRajoite;
        QOppilaitostyyppiRajoite oppilaitostyyppiRajoite = QOppilaitostyyppiRajoite.oppilaitostyyppiRajoite;

        List<String> lisatietotyyppiList = new JPAQuery<>(this.getEntityManager())
                .from(lisatietotyyppi)
//                .leftJoin(lisatietotyyppi.rajoitteet, organisaatiotyyppiRajoite)
//                .leftJoin(lisatietotyyppi.rajoitteet, oppilaitostyyppiRajoite)
                .where(anyOf(
                        organisaatiotyyppiRajoite.isNull(),//.and(oppilaitostyyppiRajoite.isNull()),
                        organisaatiotyyppiRajoite.arvo.in(organisaatiotyyppis)
//                                .and(oppilaitostyyppiRajoite.arvo.eq(oppilaitostyyppi))
                        )
//                        .or(oppilaitostyyppiRajoite.arvo.eq(oppilaitostyyppi))
                )
                .select(lisatietotyyppi.nimi).fetch();
        return new HashSet<>(lisatietotyyppiList);
    }
}
