package fi.vm.sade.varda.rekisterointi.repository;

import fi.vm.sade.varda.rekisterointi.model.Rekisterointi;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RekisterointiRepository extends CrudRepository<Rekisterointi, Long> {

    // toistaiseksi Spring Data JDBC ei loihdi automaattisesti metodeista queryjä
    // huom. referoidun taulun sarakkeille annettava selectissä etuliite "<taulunimi>_"
    String REKISTEROINTI_SELECT =
            "SELECT r.id, r.kunnat, o.ytunnus AS organisaatio_ytunnus, o.oid AS organisaatio_oid, " +
            "o.rekisterointi_id AS organisaatio_rekisterointi_id, o.alkupvm AS organisaatio_alkupvm, " +
            "o.nimi AS organisaatio_nimi, o.nimi_kieli AS organisaatio_nimi_kieli, " +
            "o.nimi_alkupvm AS organisaatio_nimi_alkupvm, o.yritysmuoto AS organisaatio_yritysmuoto, " +
            "o.tyypit AS organisaatio_tyypit, o.kotipaikka AS organisaatio_kotipaikka, o.maa AS organisaatio_maa, " +
            "o.kielet_uris AS organisaatio_kielet_uris, " +
            "y.rekisterointi_id AS organisaatio_yhteystiedot_rekisterointi_id, " +
            "y.puhelinnumero AS organisaatio_yhteystiedot_puhelinnumero, " +
            "y.sahkoposti AS organisaatio_yhteystiedot_sahkoposti, " +
            "y.posti_katuosoite AS organisaatio_yhteystiedot_posti_katuosoite, " +
            "y.posti_postinumero_uri AS organisaatio_yhteystiedot_posti_postinumero_uri, " +
            "y.posti_postitoimipaikka AS organisaatio_yhteystiedot_posti_postitoimipaikka, " +
            "y.kaynti_katuosoite AS organisaatio_yhteystiedot_kaynti_katuosoite, " +
            "y.kaynti_postinumero_uri AS organisaatio_yhteystiedot_kaynti_postinumero_uri, " +
            "y.kaynti_postitoimipaikka AS organisaatio_yhteystiedot_kaynti_postitoimipaikka, " +
            "r.toimintamuoto, r.sahkopostit, k.id AS kayttaja_id, k.etunimi AS kayttaja_etunimi, " +
            "k.sukunimi AS kayttaja_sukunimi, k.sahkoposti AS kayttaja_sahkoposti, " +
            "k.asiointikieli AS kayttaja_asiointikieli, k.saateteksti AS kayttaja_saateteksti, " +
            "r.vastaanotettu, r.tila, r.toimintamuoto, r.sahkopostit, " +
            "k.id AS kayttaja_id, k.etunimi AS kayttaja_etunimi, k.sukunimi AS kayttaja_sukunimi, " +
            "k.sahkoposti AS kayttaja_sahkoposti, k.asiointikieli AS kayttaja_asiointikieli, " +
            "k.saateteksti AS kayttaja_saateteksti, " +
            "r.vastaanotettu, r.tila " +
            "FROM rekisterointi AS r " +
            "INNER JOIN kayttaja AS k ON (k.rekisterointi = r.id) " +
            "INNER JOIN organisaatio AS o ON (o.rekisterointi_id = r.id) " +
            "INNER JOIN yhteystiedot AS y ON (y.rekisterointi_id = o.rekisterointi_id)";

    @Query(value = REKISTEROINTI_SELECT + " WHERE r.tila = :tila")
    Iterable<Rekisterointi> findByTila(@Param("tila") String tila);

    @Query(value = REKISTEROINTI_SELECT + " WHERE o.nimi LIKE '%' || :organisaatio || '%'")
    Iterable<Rekisterointi> findByOrganisaatioContaining(@Param("organisaatio") String organisaatio);

    @Query(value = REKISTEROINTI_SELECT + " WHERE r.tila = :tila AND :kunnat::text[] && (r.kunnat)")
    Iterable<Rekisterointi> findByTilaAndKunnat(@Param("tila") String tila, @Param("kunnat") String[] kunnat);

    @Query(value = REKISTEROINTI_SELECT + " WHERE r.tila = :tila AND o.nimi LIKE '%' || :organisaatio || '%'")
    Iterable<Rekisterointi> findByTilaAndOrganisaatioContaining(@Param("tila") String tila,
                                                                @Param("organisaatio") String organisaatio);

    @Query(value = REKISTEROINTI_SELECT +
            " WHERE r.tila = :tila AND :kunnat::text[] && (r.kunnat) AND o.nimi LIKE '%' || :organisaatio || '%'")
    Iterable<Rekisterointi> findByTilaAndKunnatAndOrganisaatioContaining(@Param("tila") String tila,
                                                                         @Param("kunnat") String[] kunnat,
                                                                         @Param("organisaatio") String organisaatio);


}
