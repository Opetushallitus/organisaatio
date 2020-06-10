package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.organisaatio.model.OrganisaatioLisatietotyyppi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisaatioLisatietoTyyppiRepository extends CrudRepository<OrganisaatioLisatietotyyppi, Long> {
}
