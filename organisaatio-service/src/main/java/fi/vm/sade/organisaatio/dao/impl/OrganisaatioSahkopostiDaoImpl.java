package fi.vm.sade.organisaatio.dao.impl;

import fi.vm.sade.generic.dao.AbstractJpaDAOImpl;
import fi.vm.sade.organisaatio.dao.OrganisaatioSahkopostiDao;
import fi.vm.sade.organisaatio.model.OrganisaatioSahkoposti;
import org.springframework.stereotype.Repository;

@Repository
public class OrganisaatioSahkopostiDaoImpl extends AbstractJpaDAOImpl<OrganisaatioSahkoposti, Long> implements OrganisaatioSahkopostiDao {
}
