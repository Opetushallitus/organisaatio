package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisaatioMetaDataRepository extends CrudRepository<OrganisaatioMetaData, Long> {

}
