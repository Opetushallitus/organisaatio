package fi.vm.sade.varda.rekisterointi.repository;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RekisterointiRepository extends CrudRepository<Rekisterointi, Long> {

    @Query(
            value = "SELECT r.id, r.organisaatio, r.kunnat, r.sahkopostit, r.toimintamuoto, k.id, k.etunimi, k.sukunimi, k.sahkoposti, k.asiointikieli, k.saateteksti, r.vastaanotettu, r.tila FROM rekisterointi AS r INNER JOIN kayttaja AS k ON (k.rekisterointi = r.id) WHERE r.tila = :tila::rekisteroinnin_tila",
            rowMapperClass = RekisterointiRowMapper.class)
    Iterable<Rekisterointi> findByTila(@Param("tila") String tila);

}
