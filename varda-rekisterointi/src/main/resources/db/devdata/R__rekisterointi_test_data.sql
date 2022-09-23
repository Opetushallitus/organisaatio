DELETE FROM paatos;
DELETE FROM kayttaja;
DELETE FROM yhteystiedot;
DELETE FROM organisaatio;
DELETE FROM rekisterointi;

INSERT INTO rekisterointi (id, toimintamuoto, tyyppi, kunnat, sahkopostit)
 VALUES (
     0,
     'vardatoimintamuoto_tm01',
     'varda',
     '{"Helsinki"}',
     '{"testi.yritys@testiyrit.ys"}'
 ), (
     1,
     null,
     'jotpa',
     null,
     '{"testi.yritys2@testiyrit2.ys"}'
 );

INSERT INTO organisaatio (rekisterointi_id, ytunnus, alkupvm, yritysmuoto, tyypit, kotipaikka, maa, nimi, nimi_alkupvm)
 VALUES (
     0,
     '0000000-0',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_07"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Varda-yritys',
     CURRENT_DATE
 ), (
     1,
     '0000000-1',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Jotpa-yritys',
     CURRENT_DATE
 );

INSERT INTO yhteystiedot (
    rekisterointi_id, puhelinnumero, sahkoposti, posti_katuosoite, posti_postinumero_uri, posti_postitoimipaikka,
    kaynti_katuosoite, kaynti_postinumero_uri, kaynti_postitoimipaikka
) VALUES (
    0,
    '+358101234567',
    'testi.yritys@testiyrit.ys',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091'
), (
    1,
    '+35812345678',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091'
);

INSERT INTO kayttaja (id, etunimi, sukunimi, sahkoposti, asiointikieli, saateteksti, rekisterointi)
 VALUES (
     0,
     'Testi',
     'Käyttäjä',
     'testi.kayttaja@testiyrit.ys',
     'fi',
     null,
     0
 ), (
     1,
     'kingi',
     'Käyttäjä',
     'testi.kayttaja2@testiyrit2.ys',
     'fi',
     null,
     1
 );
