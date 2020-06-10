package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganisaatioRepository extends CrudRepository<Organisaatio, Long>, OrganisaatioRepositoryCustom {
    List<Organisaatio> findByOppilaitosKoodi(String oppilaitosKoodi);
    List<Organisaatio> findByToimipisteKoodi(String toimipisteKoodi);
    List<Organisaatio> findByOid(String oid);
}

