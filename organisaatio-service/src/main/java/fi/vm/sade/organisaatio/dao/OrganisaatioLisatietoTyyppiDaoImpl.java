package fi.vm.sade.organisaatio.dao;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.model.OrganisaatioLisatietotyyppi;
import org.springframework.stereotype.Repository;


@Repository
public class OrganisaatioLisatietoTyyppiDaoImpl extends AbstractJpaDAOImpl<OrganisaatioLisatietotyyppi, Long>
        implements OrganisaatioLisatietoTyyppiDao {
}
