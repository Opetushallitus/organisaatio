package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LisatietoTyyppiRepository extends CrudRepository<Lisatietotyyppi, Long>, LisatietoTyyppiRepositoryCustom {

}
