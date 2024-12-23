COMMENT ON TABLE rekisterointi IS 'Rekisteröintihakemuksen tiedot';
COMMENT ON COLUMN rekisterointi.id IS 'Rekisteröinnin tunnus';
COMMENT ON COLUMN rekisterointi.sahkopostit IS 'Sähköpostiosoite rekisteröintiin liittyvään yhteydenottoon';
COMMENT ON COLUMN rekisterointi.vastaanotettu IS 'Rekisteröintihakemuksen vastaanottoaika';
COMMENT ON COLUMN rekisterointi.tila IS 'Rekisteröintihakemuksen tila';
COMMENT ON COLUMN rekisterointi.kunnat IS 'Kunnat, joissa rekisteröintihakemus käsitellään';
COMMENT ON COLUMN rekisterointi.toimintamuoto IS 'Rekisteröitävän varhaiskasvatustoimijan toimintamuoto';

COMMENT ON TABLE organisaatio IS 'Rekisteröitävän organisaation tiedot';
COMMENT ON COLUMN organisaatio.rekisterointi_id IS 'Viite rekisteröintihakemukseen';
COMMENT ON COLUMN organisaatio.ytunnus IS 'Organisaation y-tunnus';
COMMENT ON COLUMN organisaatio.oid IS 'Organisaation OID';
COMMENT ON COLUMN organisaatio.alkupvm IS 'Varhaiskasvatustoiminnan aloituspäivämäärä';
COMMENT ON COLUMN organisaatio.yritysmuoto IS 'Organisaation yritysmuoto';
COMMENT ON COLUMN organisaatio.tyypit IS 'Organisaation tyypit';
COMMENT ON COLUMN organisaatio.kotipaikka IS 'Organisaation kotipaikka';
COMMENT ON COLUMN organisaatio.maa IS 'Organisaation kotimaa';
COMMENT ON COLUMN organisaatio.nimi IS 'Organisaation nimi';
COMMENT ON COLUMN organisaatio.nimi_kieli IS 'Organisaation nimen kieli';
COMMENT ON COLUMN organisaatio.nimi_alkupvm IS 'Organisaation nimen voimassaolon alkupäivämäärä';
COMMENT ON COLUMN organisaatio.kielet_uris IS 'Organisaation kielet';

COMMENT ON TABLE yhteystiedot IS 'Rekisteröitävän organisaation yhteystiedot';
COMMENT ON COLUMN yhteystiedot.rekisterointi_id IS 'Viite rekisteröintihakemukseen';
COMMENT ON COLUMN yhteystiedot.puhelinnumero IS 'Organisaation puhelinnumero';
COMMENT ON COLUMN yhteystiedot.sahkoposti IS 'Organisaation sähköposti';
COMMENT ON COLUMN yhteystiedot.posti_katuosoite IS 'Postiosoitteen katuosoite';
COMMENT ON COLUMN yhteystiedot.posti_postinumero_uri IS 'Postiosoitteen postinumero';
COMMENT ON COLUMN yhteystiedot.posti_postitoimipaikka IS 'Postiosoitteen postitoimipaikka';
COMMENT ON COLUMN yhteystiedot.kaynti_katuosoite IS 'Käyntiosoitteen katuosoite';
COMMENT ON COLUMN yhteystiedot.kaynti_postinumero_uri IS 'Käyntiosoitteen postinumero';
COMMENT ON COLUMN yhteystiedot.kaynti_postitoimipaikka IS 'Käyntiosoitteen postitoimipaikka';

COMMENT ON TABLE kayttaja IS 'Rekisteröitävän organisaation pääkäyttäjä';
COMMENT ON COLUMN kayttaja.id IS 'Käyttäjän tunniste';
COMMENT ON COLUMN kayttaja.etunimi IS 'Käyttäjän etunimi';
COMMENT ON COLUMN kayttaja.sukunimi IS 'Käyttäjän sukunimi';
COMMENT ON COLUMN kayttaja.sahkoposti IS 'Käyttäjän sähköposti';
COMMENT ON COLUMN kayttaja.asiointikieli IS 'Käyttäjän asiointikieli';
COMMENT ON COLUMN kayttaja.saateteksti IS 'Käyttäjälle lähetettävä saateteksti';
COMMENT ON COLUMN kayttaja.rekisterointi IS 'Viite rekisteröintihakemukseen';

COMMENT ON TABLE paatos IS 'Rekisteröintihakemukselle annettu päätös';
COMMENT ON COLUMN paatos.rekisterointi_id IS 'Viite rekisteröintihakemukseen';
COMMENT ON COLUMN paatos.hyvaksytty IS 'Hyväksytty: kyllä/ei';
COMMENT ON COLUMN paatos.paatetty IS 'Päätöksen aikaleima';
COMMENT ON COLUMN paatos.perustelu IS 'Päätöksen perustelu';
COMMENT ON COLUMN paatos.paattaja_oid IS 'Päätöksen tekijän OID';
