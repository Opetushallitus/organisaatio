-- noinspection SqlDialectInspectionForFile
-- noinspection SqlNoDataSourceInspectionForFile
INSERT INTO MONIKIELINENTEKSTI(ID, VERSION) VALUES
(1, 0),
(2, 0),
(3, 0),
(4, 0),
(5, 0),
(6, 0),
(7, 0),
(8, 0),
(9, 0),
(11, 0),
(12, 0),
(1000, 0);

INSERT INTO MONIKIELINENTEKSTI_VALUES(ID, VALUE, KEY) VALUES
(1, 'opetushallitus', 'fi'),
(2, 'root test koulutustoimija', 'fi'),
(2, 'root test utbildningsoperator', 'sv'),
(3, 'node1 asd', 'fi'),
(4, 'node2 foo', 'fi'),
(5, 'node22 foo bar', 'fi'),
(6, 'node23 foo bar', 'fi'),
(7, 'root2 test2 koulutustoimija2', 'fi'),
(8, 'nodex bar', 'fi'),
(9, 'node231 foo bar', 'sv'),
(11, 'Varhaiskasvatuksen toimipiste', 'fi'),
(12, 'Liitostesti', 'fi'),
(1000, 'Hakutoimiston nimi FI', 'kieli_fi#1'),
(1000, 'Hakutoimiston nimi EN', 'kieli_en#1');

INSERT INTO ORGANISAATIOMETADATA(ID, VERSION, LUONTIPVM, MUOKKAUSPVM, HAKUTOIMISTONIMI_ID) VALUES
(1, 1, DATE '1970-01-01', DATE '1970-01-01', 1000),
(2, 1, DATE '1970-01-01', DATE '1970-01-01', 1000);

INSERT INTO YHTEYSTIETO(DTYPE, ID, VERSION, YHTEYSTIETOOID, OSOITE, OSOITETYYPPI, POSTINUMERO, POSTITOIMIPAIKKA, KIELI) VALUES
('Osoite', 1000, 1, '1.2.2004.4', 'Hassuttimenkatu 2', 'kaynti', 'posti_10000', 'Juupajoki', 'kieli_fi#1'),
('Osoite', 1001, 1, '1.2.2004.5', 'Hassuttimenkatu 2', 'posti', 'posti_10000', 'Juupajoki', 'kieli_fi#1'),
('Osoite', 1002, 1, '1.2.2004.6', 'Hassutingatan 2', 'kaynti', 'posti_10000', 'Juupajoki', 'kieli_sv#1'),
('Osoite', 1003, 1, '1.2.2004.7', 'Hassuttimenkatu 2, 10000 Juupajoki, Finland', 'ulkomainen_kaynti', null, null, 'kieli_en#1'),
('Osoite', 1004, 1, '1.2.2004.8', 'Hassuttimenkatu 2, 10000 Juupajoki, Finland', 'ulkomainen_posti', null, null, 'kieli_en#1'),
('Osoite', 1005, 1, '1.2.2004.9', 'Hassuttimenkatu 2, 10000 Juupajoki, Finland', 'kaynti', null, null, 'kieli_en#1'),
('Osoite', 1006, 1, '1.2.2004.10', 'Hassuttimenkatu 2, 10000 Juupajoki, Finland', 'posti', null, null, 'kieli_en#1');

INSERT INTO YHTEYSTIETO(DTYPE, ID, VERSION, YHTEYSTIETOOID, WWWOSOITE, KIELI) VALUES
('Www', 1101, 1, '1.2.2004.100', 'http://www.foo.fi', 'kieli_fi#1'),
('Www', 1102, 1, '1.2.2004.101', 'http://www.foo.fi/en', 'kieli_en#1');

INSERT INTO YHTEYSTIETO(DTYPE, ID, VERSION, YHTEYSTIETOOID, PUHELINNUMERO, TYYPPI, KIELI) VALUES
('Puhelinnumero', 1201, 1, '1.2.2004.201', '123456789', 'puhelin', 'kieli_fi#1');

INSERT INTO YHTEYSTIETO(DTYPE, ID, VERSION, YHTEYSTIETOOID, EMAIL, KIELI) VALUES
('Email', 1301, 1, '1.2.2004.301', 'foo@bar.com', 'kieli_fi#1');

INSERT INTO ORGANISAATIOMETADATA_YHTEYSTIETO(ORGANISAATIOMETADATA_ID, YHTEYSTIEDOT_ID) VALUES
(1, 1000),
(1, 1001),
(1, 1002),
(1, 1003),
(1, 1004),
(2, 1005),
(2, 1006),
(1, 1101),
(1, 1102),
(1, 1201),
(1, 1301);

INSERT INTO ORGANISAATIO(ID, VERSION, PAIVITYSPVM, ALKUPVM, DOMAINNIMI, KOTIPAIKKA, LAKKAUTUSPVM, MAA, NIMIHAKU, OID, OPETUSPISTEENJARJNRO, OPPILAITOSKOODI, OPPILAITOSTYYPPI, ORGANISAATIOPOISTETTU, PAIVITTAJA, PARENTIDPATH, TOIMIPISTEKOODI, TUONTIPVM, VIRASTOTUNNUS, YHTEISHAUNKOULUKOODI, YRITYSMUOTO, YTJPAIVITYSPVM, YTUNNUS, KUVAUS_MKT, METADATA_ID, NIMI_MKT, PIILOTETTU) VALUES
(1, 0, DATE '2004-06-29', DATE '2004-06-29', NULL, 'Helsinki', NULL, NULL, 'opetushallitus', '1.2.246.562.24.00000000001', NULL, NULL, NULL, FALSE, NULL, NULL, NULL, NULL, NULL, NULL, 'oy', NULL, NULL, NULL, NULL, 1, FALSE),
(2, 2, DATE '2004-08-08',DATE '2004-08-08', NULL, 'Helsinki', NULL, NULL, 'root test koulutustoimija', '1.2.2004.1', NULL, '12345', NULL, FALSE, NULL, '|1|', NULL, NULL, NULL, NULL, 'oy', NULL, '2255802-1', NULL, NULL, 2, FALSE),
(3, 2, DATE '2013-06-29', '2013-06-29', NULL, 'Helsinki', NULL, NULL, 'node1 asd', '1.2.2004.2', 1, NULL, 'oppilaitostyyppi_41#1', FALSE, NULL, '|1|2|', '123451', NULL, NULL, NULL, 'oy', NULL, '1234567-2', NULL, NULL, 3, FALSE),
(4, 2, NULL, NULL, NULL, 'Helsinki', NULL, NULL, 'node2 foo', '1.2.2004.3', 2, NULL, 'oppilaitostyyppi_42#1', FALSE, NULL, '|1|2|', '123452', NULL, NULL, NULL, 'oy', NULL, '1234567-3', NULL, 1, 4, FALSE),
(5, 2, NULL, NULL, NULL, 'Helsinki', NULL, NULL, 'node22 foo bar', '1.2.2004.4', NULL, NULL, 'oppilaitostyyppi_42#1', FALSE, NULL, '|1|2|4|', NULL, NULL, NULL, NULL, 'oy', NULL, '1234567-4', NULL, NULL, 5, FALSE),
(6, 2, DATE '2010-08-29', DATE '2010-08-29', NULL, 'Helsinki', NULL, NULL, 'node23 foo bar', '1.2.2005.4', NULL, NULL, 'oppilaitostyyppi_42#1', FALSE, NULL, '|1|2|4|', NULL, NULL, NULL, NULL, 'oy', NULL, '1234568-4', NULL, NULL, 6, FALSE),
(7, 2, DATE '2006-06-29', DATE '2006-06-29', NULL, 'Helsinki', NULL, NULL, 'root2 test2 koulutustoimija2', '1.2.2004.5', NULL, NULL, NULL, FALSE, NULL, '|1|', NULL, NULL, NULL, NULL, 'oy', NULL, '1492449-0', NULL, NULL, 7, FALSE),
(8, 1, DATE '2008-04-11', DATE '2008-04-11', NULL, 'Helsinki', DATE '2011-06-29', NULL, 'nodex bar', '1.2.2004.6', NULL, NULL, 'oppilaitostyyppi_41#1', FALSE, NULL, '|1|7|', NULL, NULL, NULL, NULL, 'oy', NULL, '1234567-6', NULL, NULL, 8, FALSE),
(9, 2, DATE '1960-02-16', DATE '1960-02-16', NULL, 'Helsinki', NULL, NULL, 'node231 foo bar piilotettu', '1.2.2005.5', NULL, NULL, 'oppilaitostyyppi_42#1', FALSE, NULL, '|1|2|4|5|', NULL, NULL, NULL, NULL, 'oy', NULL, '1730639-9', NULL, NULL, 9, FALSE),
(10, 2, NULL, NULL, NULL, 'Helsinki', NULL, NULL, 'mixed osoitetyyppi organisaatio', '1.2.8000.1', NULL, NULL, 'oppilaitostyyppi_42#1', FALSE, NULL, '|1|', NULL, NULL, NULL, NULL, 'oy', NULL, '1234569-5', NULL, 2, 6, FALSE),
(11, 0, NULL, NULL, NULL, 'Helsinki', NULL, NULL, 'varhaiskasvatuksen', '1.2.2020.1', NULL, NULL, NULL, FALSE, NULL, '|1|10|', NULL, NULL, NULL, NULL, 'oy', NULL, '8538031-2', NULL, NULL, 11, TRUE),
(12, 0, NULL, NULL, NULL, 'Helsinki', NULL, NULL, 'varhaiskasvatuksen', '1.2.8001.1', NULL, NULL, NULL, FALSE, NULL, NULL, NULL, NULL, NULL, NULL, 'oy', NULL, '6165182-7', NULL, NULL, 12, FALSE);

INSERT INTO ORGANISAATIO_PARENT_OIDS(ORGANISAATIO_ID, PARENT_OID, PARENT_POSITION) VALUES
(2, '1.2.246.562.24.00000000001', 0),
(3, '1.2.2004.1', 0),
(3, '1.2.246.562.24.00000000001', 1),
(4, '1.2.2004.1', 0),
(4, '1.2.246.562.24.00000000001', 1),
(5, '1.2.2004.3', 0),
(5, '1.2.2004.1', 1),
(5, '1.2.246.562.24.00000000001', 2),
(6, '1.2.2004.3', 0),
(6, '1.2.2004.1', 1),
(6, '1.2.246.562.24.00000000001', 2),
(7, '1.2.246.562.24.00000000001', 0),
(8, '1.2.2004.5', 0),
(8, '1.2.246.562.24.00000000001', 1),
(9, '1.2.2004.4', 0),
(9, '1.2.2004.3', 1),
(9, '1.2.2004.1', 2),
(9, '1.2.246.562.24.00000000001', 3),
(10, '1.2.246.562.24.00000000001', 0),
(11, '1.2.8000.1', 0),
(11, '1.2.246.562.24.00000000001', 1);

INSERT INTO YHTEYSTIETO(DTYPE, ID, VERSION, KIELI, YHTEYSTIETOOID, COORDINATETYPE, EXTRARIVI, LAT, LNG, MAA, OSAVALTIO, OSOITE, OSOITETYYPPI, POSTINUMERO, POSTITOIMIPAIKKA, YTJPAIVITYSPVM, PUHELINNUMERO, TYYPPI, EMAIL, WWWOSOITE, ORGANISAATIO_ID) VALUES
('Osoite', 1, 0, 'kieli_fi#1', '14175174062990.39764983115354857', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 1),
('Osoite', 2, 0, 'kieli_fi#1', '14175174062990.620466503755304', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 1),
('Puhelinnumero', 3, 0, 'kieli_fi#1', '14175174062990.8204006983929734', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 1),
('Www', 5, 0, 'kieli_fi#1', '14175174062990.27296380481785454', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 1),
('Email', 6, 0, 'kieli_fi#1', '14175174062990.12215188486861617', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 1),
('Osoite', 7, 0, 'kieli_fi#1', '14175174064390.7485633929107891', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 2),
('Osoite', 8, 0, 'kieli_fi#1', '14175174064390.6645604532079388', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 2),
('Puhelinnumero', 9, 0, 'kieli_fi#1', '14175174064390.759769529586222', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 2),
('Www', 11, 0, 'kieli_fi#1', '14175174064390.5602732430162826', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 2),
('Email', 12, 0, 'kieli_fi#1', '14175174064390.5149287835370774', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 2),
('Osoite', 13, 0, 'kieli_fi#1', '14175174064820.8648056949629902', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 3),
('Osoite', 14, 0, 'kieli_fi#1', '14175174064820.7481032001042686', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 3),
('Puhelinnumero', 15, 0, 'kieli_fi#1', '14175174064820.023236488244545495', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 3),
('Www', 17, 0, 'kieli_fi#1', '14175174064820.6076819639840219', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 3),
('Email', 18, 0, 'kieli_fi#1', '14175174064820.8327622948696073', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 3),
('Osoite', 19, 0, 'kieli_fi#1', '14175174065080.4219192693239784', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 4),
('Osoite', 20, 0, 'kieli_fi#1', '14175174065080.550941563558955', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 4),
('Puhelinnumero', 21, 0, 'kieli_fi#1', '14175174065080.10010532775601144', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 4),
('Www', 23, 0, 'kieli_fi#1', '14175174065080.48943728578096246', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 4),
('Email', 24, 0, 'kieli_fi#1', '14175174065080.4533891151600614', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 4),
('Osoite', 25, 0, 'kieli_fi#1', '14175174065360.6247828466966071', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 5),
('Osoite', 26, 0, 'kieli_fi#1', '14175174065360.9464436263842728', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 5),
('Puhelinnumero', 27, 0, 'kieli_fi#1', '14175174065360.2214006361289652', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 5),
('Www', 29, 0, 'kieli_fi#1', '14175174065360.6067320470604116', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 5),
('Email', 30, 0, 'kieli_fi#1', '14175174065360.7605481160245522', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 5),
('Osoite', 31, 0, 'kieli_fi#1', '14175174065630.9590734144321167', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 6),
('Osoite', 32, 0, 'kieli_fi#1', '14175174065630.7513744924059244', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 6),
('Puhelinnumero', 33, 0, 'kieli_fi#1', '14175174065630.6480495083244752', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 6),
('Www', 35, 0, 'kieli_fi#1', '14175174065630.6106530088032424', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 6),
('Email', 36, 0, 'kieli_fi#1', '14175174065630.7174733712778176', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 6),
('Osoite', 37, 0, 'kieli_fi#1', '14175174066030.7317595529754113', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 7),
('Osoite', 38, 0, 'kieli_fi#1', '14175174066030.21304763052326048', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 7),
('Puhelinnumero', 39, 0, 'kieli_fi#1', '14175174066030.517169300422662', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 7),
('Www', 41, 0, 'kieli_fi#1', '14175174066030.29118402444503866', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 7),
('Email', 42, 0, 'kieli_fi#1', '14175174066030.945052372475997', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 7),
('Osoite', 43, 0, 'kieli_fi#1', '14175174066260.2634494684757879', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 8),
('Osoite', 44, 0, 'kieli_fi#1', '14175174066260.1577075445656858', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 1', 'kaynti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 8),
('Puhelinnumero', 45, 0, 'kieli_fi#1', '14175174066260.08834572445754851', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '12345', 'puhelin', NULL, NULL, 8),
('Www', 47, 0, 'kieli_fi#1', '14175174066260.684789002737332', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'http://www.oph.fi', 8),
('Email', 48, 0, 'kieli_fi#1', '14175174066260.7840178553027418', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'testi@oph.fi', NULL, 8),
('Osoite', 49, 0, 'kieli_sv#1', '14175174066260.2634494684757834', NULL, NULL, NULL, NULL, NULL, NULL, 'Mannerheimintie 2', 'posti', 'posti_00100', 'Helsinki', NULL, NULL, NULL, NULL, NULL, 9);

INSERT INTO ORGANISAATIO_NIMI(ID, VERSION, ALKUPVM, PAIVITTAJA, NIMI_MKT, ORGANISAATIO_ID) VALUES
(1, 0, DATE '1970-01-01', NULL, 1, 1),
(2, 0, DATE '1970-01-01', NULL, 2, 2),
(3, 0, DATE '1970-01-01', NULL, 3, 3),
(4, 0, DATE '1970-01-01', NULL, 4, 4),
(5, 0, DATE '1970-01-01', NULL, 5, 5),
(6, 0, DATE '1970-01-01', NULL, 6, 6),
(7, 0, DATE '1970-01-01', NULL, 7, 7),
(8, 0, DATE '1970-01-01', NULL, 8, 8),
(9, 0, DATE '1970-01-01', NULL, 9, 9);

INSERT INTO ORGANISAATIO_TYYPIT(ORGANISAATIO_ID, TYYPIT) VALUES
(1, 'organisaatiotyyppi_01'),
(2, 'organisaatiotyyppi_01'),
(3, 'organisaatiotyyppi_03'),
(3, 'organisaatiotyyppi_02'),
(3, 'organisaatiotyyppi_04'),
(4, 'organisaatiotyyppi_03'),
(4, 'organisaatiotyyppi_02'),
(4, 'organisaatiotyyppi_04'),
(5, 'organisaatiotyyppi_03'),
(5, 'organisaatiotyyppi_02'),
(5, 'organisaatiotyyppi_04'),
(6, 'organisaatiotyyppi_03'),
(6, 'organisaatiotyyppi_02'),
(6, 'organisaatiotyyppi_04'),
(7, 'organisaatiotyyppi_01'),
(8, 'organisaatiotyyppi_03'),
(8, 'organisaatiotyyppi_02'),
(8, 'organisaatiotyyppi_04'),
(9, 'organisaatiotyyppi_06'),
(10, 'organisaatiotyyppi_02'),
(10, 'organisaatiotyyppi_07'),
(11, 'organisaatiotyyppi_08');

INSERT INTO ORGANISAATIOSUHDE(ID, VERSION, ALKUPVM, LOPPUPVM, OPETUSPISTEENJARJNRO, SUHDETYYPPI, CHILD_ID, PARENT_ID) VALUES
(1, 0, TIMESTAMP '2014-12-02 12:50:06.46', NULL, NULL, 'HISTORIA', 2, 1),
(2, 0, TIMESTAMP '2014-12-02 12:50:06.499', NULL, NULL, 'HISTORIA', 3, 2),
(3, 0, TIMESTAMP '2014-12-02 12:50:06.528', NULL, NULL, 'HISTORIA', 4, 2),
(4, 0, TIMESTAMP '2014-12-02 12:50:06.555', NULL, NULL, 'HISTORIA', 5, 4),
(5, 0, TIMESTAMP '2014-12-02 12:50:06.595', NULL, NULL, 'HISTORIA', 6, 4),
(6, 0, TIMESTAMP '2014-12-02 12:50:06.62', NULL, NULL, 'HISTORIA', 7, 1),
(7, 0, TIMESTAMP '2014-12-02 12:50:06.645', NULL, NULL, 'HISTORIA', 8, 7),
(8, 0, TIMESTAMP '2014-12-02 12:50:06.645', NULL, NULL, 'HISTORIA', 9, 5),
(9, 0, TIMESTAMP '2014-12-02 12:50:06.645', NULL, NULL, 'HISTORIA', 10, 1),
(10, 0, TIMESTAMP '2014-12-02 12:50:06.645', NULL, NULL, 'HISTORIA', 11, 1),
(11, 0, TIMESTAMP '2014-12-02 12:50:06.645', NULL, NULL, 'LIITOS', 12, 11);

INSERT INTO organisaatio_kielet(organisaatio_id, kielet) VALUES
(10, 'oppilaitoksenopetuskieli_1#1'),
(10, 'oppilaitoksenopetuskieli_2#1'),
(10, 'oppilaitoksenopetuskieli_3#1');
