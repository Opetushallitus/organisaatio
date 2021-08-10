package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Lisatietotyyppi;
import fi.vm.sade.organisaatio.model.OrganisaatioMetaData;
import fi.vm.sade.organisaatio.model.OrganisaatioNimi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisaatioMetaDataRepository extends CrudRepository<OrganisaatioMetaData, Long> {

}
