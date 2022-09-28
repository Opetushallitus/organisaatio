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
     '{"testi.varda@testiyrit.ys"}'
 ), (
     1,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit2.ys"}'
), (
     6,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit3.ys"}'
), (
     7,
     null,
     'jotpa',
     null,
     '{"testi.jotpa@testiyrit4.ys"}'
);

INSERT INTO rekisterointi (id, toimintamuoto, tyyppi, kunnat, sahkopostit, tila)
VALUES (
           2,
           'vardatoimintamuoto_tm01',
           'varda',
           '{"Helsinki"}',
           '{"testi.varda.hyvaksytty@varda.com"}',
           'HYVAKSYTTY'
       ),  (
            3,
            'vardatoimintamuoto_tm01',
            'varda',
            '{"Helsinki"}',
            '{"testi.varda.hylatty@varda.com"}',
            'HYLATTY'
        ), (
           4,
           null,
           'jotpa',
           null,
           '{"testi.jotpa.hyvaksytty@jotpa.com"}',
           'HYVAKSYTTY'
       );

INSERT INTO paatos (rekisterointi_id, hyvaksytty, paatetty, paattaja_oid, perustelu)
VALUES (
           2,
           true,
           CURRENT_DATE,
           '1.2.3.4.5',
           'Hyväksytty päätös'
       ), (
           3,
           false,
           CURRENT_DATE,
           '5.4.3.2.1',
           'Hylätty päätös'
       ),(
    4,
        true,
    CURRENT_DATE,
    '1.2.3.4.5',
    'Hyväksytty päätös'
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
 ), (
     2,
     '0000000-2',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Varda-yritys hyväksytty',
     CURRENT_DATE
 ),(
    3,
    '0000000-3',
    CURRENT_DATE,
    'yritysmuoto_26',
    '{"organisaatiotyyppi_01"}',
    'kunta_091',
    'maatjavaltiot1_fin',
    'Varda-yritys hylätty',
    CURRENT_DATE
), (
     4,
     '0000000-4',
     CURRENT_DATE,
     'yritysmuoto_26',
     '{"organisaatiotyyppi_01"}',
     'kunta_091',
     'maatjavaltiot1_fin',
     'Jotpa-yritys hyväksytty',
     CURRENT_DATE
 ), (
    6,
    '0000000-6',
    CURRENT_DATE,
    'yritysmuoto_26',
    '{"organisaatiotyyppi_01"}',
    'kunta_091',
    'maatjavaltiot1_fin',
    'Jotpa-yritys 2',
    CURRENT_DATE
), (
    7,
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
    0,
    '+358101234567',
    'testi.yritys@testiyrit.ys',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 14', 'posti_00530', 'kunta_091'
), (
    1,
    '+35812112121',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 12', 'posti_00530', 'kunta_091'
),(
    2,
    '+3585021211',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 11', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 11', 'posti_00530', 'kunta_091'
),(
    3,
    '+3581132445',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 10', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 10', 'posti_00530', 'kunta_091'
),(
    4,
    '+358124563',
    'testi.yritys2@testiyrit2.ys',
    'Haapaniemenkatu 9', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 9', 'posti_00530', 'kunta_091'
),(
    6,
    '+3581234353',
    'testi.yritys6@testiyrit3.ys',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091'
),(
    7,
    '+3581234353',
    'testi.yritys7@testiyrit4.ys',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091',
    'Haapaniemenkatu 8', 'posti_00530', 'kunta_091'
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
     'testi.kayttaja1@testiyrit2.ys',
     'fi',
     null,
     1
 ), (
    2,
    'ruotsi',
    'Käyttäjä',
    'testi.kayttaja2@testiyrit2.ys',
    'sv',
    null,
    2
), (
    3,
    'Taas',
    'Käyttäjä',
    'testi.kayttaja3@testiyrit2.ys',
    'fi',
    null,
    3
),(
     4,
     'neljäs',
     'Käyttäjä',
     'testi.kayttaja4@testiyrit2.ys',
     'fi',
     null,
     4
 ),(
     6,
     'Saate 6',
     'Käyttäjä',
     'testi.kayttaja75@testiyrit3.ys',
     'fi',
     'saateteksti testi',
     6
 ),(
     7,
     'Saate 7',
     'Käyttäjä',
     'testi.kayttaja7@testiyrit4.ys',
     'fi',
     'saateteksti testi',
     7
 );
