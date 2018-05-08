package fi.vm.sade.organisaatio.dao.impl;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.LisatietoTyyppiDao;
import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import org.springframework.stereotype.Repository;

@Repository
public class LisatietoTyyppiDaoImpl  extends AbstractJpaDAOImpl<Lisatietotyyppi, Long> implements LisatietoTyyppiDao {

}
