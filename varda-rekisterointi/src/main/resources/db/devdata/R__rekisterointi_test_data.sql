DELETE FROM paatos;
DELETE FROM kayttaja;
DELETE FROM yhteystiedot;
DELETE FROM organisaatio;
DELETE FROM rekisterointi;


INSERT INTO rekisterointi (id, toimintamuoto, tyyppi, kunnat, sahkopostit)
 VALUES (
     1000,
     'vardatoimintamuoto_tm01',
     'varda',
     '{"Helsinki"}',
     '{"testi.varda@testiyrit.ys"}'
 ), (
     1001,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit2.ys"}'
), (
     1006,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit3.ys"}'
), (
     1007,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit4.ys"}'
);

INSERT INTO rekisterointi (id, toimintamuoto, tyyppi, kunnat, sahkopostit, tila)
VALUES (
           1002,
           'vardatoimintamuoto_tm01',
           'varda',
           '{"Helsinki"}',
           '{"testi.varda.hyvaksytty@varda.com"}',
           'HYVAKSYTTY'
       ),  (
            1003,
            'vardatoimintamuoto_tm01',
            'varda',
            '{"Helsinki"}',
            '{"testi.varda.hylatty@varda.com"}',
            'HYLATTY'
        ), (
           1004,
           null,
           'jotpa',
           null,
           '{"testi.jotpa.hyvaksytty@jotpa.com"}',
           'HYVAKSYTTY'
       );

INSERT INTO paatos (rekisterointi_id, hyvaksytty, paatetty, paattaja_oid, perustelu)
VALUES (
           1002,
           true,
           CURRENT_DATE,
           '1.2.3.4.5',
           'Hyväksytty päätös'
       ), (
           1003,
           false,
           CURRENT_DATE,
           '5.4.3.2.1',
           'Hylätty päätös'
       ),(
    1004,
        true,
    CURRENT_DATE,
    '1.2.3.4.5',
    'Hyväksytty päätös'
);

INSERT INTO organisaatio (rekisterointi_id, ytunnus, alkupvm, yritysmuoto, tyypit, kotipaikka, maa, nimi, nimi_alkupvm)
 VALUES (
     1000,
     '0000000-0',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_07"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Varda-yritys1',
     CURRENT_DATE
 ), (
     1001,
     '0000000-1',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Jotpa-yritys',
     CURRENT_DATE
 ), (
     1002,
     '0000000-2',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Varda-yritys hyväksytty',
     CURRENT_DATE
 ),(
    1003,
    '0000000-3',
    CURRENT_DATE,
    'yritysmuoto_26',
    '{"organisaatiotyyppi_01"}',
    'kunta_091',
    'maatjavaltiot1_fin',
    'Varda-yritys hylätty',
    CURRENT_DATE
), (
     1004,
     '0000000-4',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Jotpa-yritys hyväksytty',
     CURRENT_DATE
 ), (
    1006,
    '0000000-6',
    CURRENT_DATE,
    'yritysmuoto_26',
    '{"organisaatiotyyppi_01"}',
    'kunta_091',
    'maatjavaltiot1_fin',
    'Jotpa-yritys 2',
    CURRENT_DATE
), (
    1007,
    '0000000-7',
    CURRENT_DATE,
    'yritysmuoto_26',
    '{"organisaatiotyyppi_01"}',
    'kunta_091',
    'maatjavaltiot1_fin',
    'Jotpa-yritys 3',
    CURRENT_DATE
);

INSERT INTO yhteystiedot (
    rekisterointi_id, puhelinnumero, sahkoposti, posti_katuosoite, posti_postinumero_uri, posti_postitoimipaikka,
    kaynti_katuosoite, kaynti_postinumero_uri, kaynti_postitoimipaikka
) VALUES (
    1000,
    '+358101234567',
    'testi.yritys@testiyrit.ys',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091'
), (
    1001,
    '+35812112121',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091'
),(
    1002,
    '+3585021211',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 11', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 11', 'posti_00530', 'kunta_091'
),(
    1003,
    '+3581132445',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 10', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 10', 'posti_00530', 'kunta_091'
),(
    1004,
    '+358124563',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 9', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 9', 'posti_00530', 'kunta_091'
),(
    1006,
    '+3581234353',
    'testi.yritys6@testiyrit3.ys',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091'
),(
    1007,
    '+3581234353',
    'testi.yritys7@testiyrit4.ys',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091'
);

INSERT INTO kayttaja (id, etunimi, sukunimi, sahkoposti, asiointikieli, saateteksti, rekisterointi)
 VALUES (
     10,
     'Testi',
     'Käyttäjä',
     'testi.kayttaja@testiyrit.ys',
     'fi',
     null,
     1000
 ), (
     11,
     'kingi',
     'Käyttäjä',
     'testi.kayttaja1@testiyrit2.ys',
     'fi',
     null,
     1001
 ), (
    12,
    'ruotsi',
    'Käyttäjä',
    'testi.kayttaja2@testiyrit2.ys',
    'sv',
    null,
    1002
), (
    13,
    'Taas',
    'Käyttäjä',
    'testi.kayttaja3@testiyrit2.ys',
    'fi',
    null,
    1003
),(
     14,
     'neljäs',
     'Käyttäjä',
     'testi.kayttaja4@testiyrit2.ys',
     'fi',
     null,
     1004
 ),(
     16,
     'Saate 6',
     'Käyttäjä',
     'testi.kayttaja75@testiyrit3.ys',
     'fi',
     'saateteksti testi',
     1006
 ),(
     17,
     'Saate 7',
     'Käyttäjä',
     'testi.kayttaja7@testiyrit4.ys',
     'fi',
     'saateteksti testi',
     1007
 );
