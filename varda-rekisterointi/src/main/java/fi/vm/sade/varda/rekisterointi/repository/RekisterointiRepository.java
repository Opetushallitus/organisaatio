package fi.vm.sade.varda.rekisterointi.repository;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RekisterointiRepository extends CrudRepository<Rekisterointi, Long> {

    // toistaiseksi Spring Data JDBC ei loihdi automaattisesti metodeista queryjä
    // huom. referoidun taulun sarakkeille annettava selectissä etuliite "<taulunimi>_"
    @Query(value = "SELECT r.id, r.kunnat, r.organisaatio, r.sahkopostit, r.toimintamuoto, k.id AS kayttaja_id, k.etunimi AS kayttaja_etunimi, k.sukunimi AS kayttaja_sukunimi, k.sahkoposti AS kayttaja_sahkoposti, k.asiointikieli AS kayttaja_asiointikieli, k.saateteksti AS kayttaja_saateteksti, r.vastaanotettu, r.tila FROM rekisterointi AS r INNER JOIN kayttaja AS k ON (k.rekisterointi = r.id) WHERE r.tila = :tila")
    Iterable<Rekisterointi> findByTila(@Param("tila") String tila); // eikä tunnu hanskaavan queryssä enumin muunnosta, joten...

}
