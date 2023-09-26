package fi.vm.sade.organisaatio.repository;

import fi.vm.sade.organisaatio.model.Organisaatio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganisaatioRepository extends JpaRepository<Organisaatio, Long>, OrganisaatioRepositoryCustom {
    List<Organisaatio> findByOppilaitosKoodi(String oppilaitosKoodi);

    List<Organisaatio> findByToimipisteKoodi(String toimipisteKoodi);

    Organisaatio findFirstByOid(String oid);

    @Query(
            value = "SELECT * FROM organisaatio JOIN organisaatio_tyypit ON (organisaatio.id = organisaatio_tyypit.organisaatio_id AND :organisaatiotyyppi = organisaatio_tyypit.tyypit)",
            nativeQuery = true
    )
    List<Organisaatio> findByOrganisaatiotyyppi(String organisaatiotyyppi);
}

