
INSERT INTO "public"."monikielinenteksti" ("id", "version") VALUES ('1', '0'),
('2', '0');

INSERT INTO "public"."monikielinenteksti_values" ("id", "value", "key") VALUES ('2', 'Finnish National Agency for Education', 'en'),
('2', 'Opetushallitus', 'fi'),
('2', 'Utbildningsstyrelsen', 'sv');

INSERT INTO "organisaatio" ("id", "version", "alkupvm", "domainnimi", "kotipaikka", "lakkautuspvm", "maa", "nimihaku", "oid", "opetuspisteenjarjnro", "oppilaitoskoodi", "oppilaitostyyppi", "organisaatiopoistettu", "paivittaja", "paivityspvm", "parentidpath", "piilotettu", "tarkastuspvm", "toimipistekoodi", "tuontipvm", "virastotunnus", "yhteishaunkoulukoodi", "yritysmuoto", "ytjkieli", "ytjpaivityspvm", "ytunnus", "kuvaus_mkt", "metadata_id", "nimi_mkt", "varhaiskasvatuksen_toimipaikka_tiedot_id") VALUES ('1', '1', '1970-01-01', NULL, NULL, NULL, 'maatjavaltiot1_fin', ',Opetushallitus,Utbildningsstyrelsen,Finnish National Agency for Education', '1.2.246.562.10.00000000001', NULL, NULL, NULL, 'f', '1.2.246.562.10.00000000001', '2021-09-15 11:48:14.8', '', 'f', NULL, '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', NULL, '2', NULL);

INSERT INTO "public"."organisaatio_nimi" ("id", "version", "alkupvm", "paivittaja", "nimi_mkt", "organisaatio_id") VALUES ('1', '0', '1970-01-01', NULL, '2', '1');


INSERT INTO "public"."organisaatio_tyypit" ("organisaatio_id", "tyypit") VALUES ('1', 'organisaatiotyyppi_05');
