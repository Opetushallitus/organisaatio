package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganisaatioRepository extends JpaRepository<Organisaatio, Long>, OrganisaatioRepositoryCustom {
    List<Organisaatio> findByOppilaitosKoodi(String oppilaitosKoodi);
    List<Organisaatio> findByToimipisteKoodi(String toimipisteKoodi);
    Organisaatio findFirstByOid(String oid);
}

