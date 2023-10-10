--
-- OPH-647
--

-- 81843 / 1.2.246.562.10.756472466210
INSERT INTO organisaatio (id, version, nimi_mkt, piilotettu) VALUES (81843, 0, 0, false) ON CONFLICT DO NOTHING;
UPDATE organisaatio_nimi SET alkupvm = '1992-08-01' WHERE organisaatio_id = 81843;
INSERT INTO monikielinenteksti (id, version) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0);
INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 1, '1992-01-01', 81843, (SELECT MAX(id) FROM monikielinenteksti));
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Rauman iltalukio', 'fi');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Rauman iltalukio', 'sv');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Rauman iltalukio', 'en');

-- 36878 / 1.2.246.562.10.53442802061
INSERT INTO organisaatio (id, version, nimi_mkt, piilotettu) VALUES (36878, 0, 0, false) ON CONFLICT DO NOTHING;
UPDATE organisaatio_nimi SET alkupvm = '2006-08-01' WHERE organisaatio_id = 36878 AND alkupvm = '1992-01-01';
INSERT INTO monikielinenteksti (id, version) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0);
INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 1, '1992-01-01', 36878, (SELECT MAX(id) FROM monikielinenteksti));
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Toijalan lukio', 'fi');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Toijalan lukio', 'sv');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Toijalan lukio', 'en');

-- 7942 / 1.2.246.562.10.94415649832
INSERT INTO organisaatio (id, version, nimi_mkt, piilotettu) VALUES (7942, 0, 0, false) ON CONFLICT DO NOTHING;
UPDATE organisaatio_nimi SET alkupvm = '1994-08-01' WHERE organisaatio_id = 7942 AND alkupvm = '1992-01-01';
INSERT INTO monikielinenteksti (id, version) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0);
INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 1, '1992-01-01', 7942, (SELECT MAX(id) FROM monikielinenteksti));
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Länsi-Vantaan iltalukio', 'fi');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Länsi-Vantaan iltalukio', 'sv');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Länsi-Vantaan iltalukio', 'en');

-- 14040 / 1.2.246.562.10.43886782945
INSERT INTO organisaatio (id, version, nimi_mkt, piilotettu) VALUES (14040, 0, 0, false) ON CONFLICT DO NOTHING;
UPDATE monikielinenteksti_values set value = 'Riihimäen iltalukio' where id = (select nimi_mkt from organisaatio_nimi where alkupvm = '1992-01-01' and organisaatio_id = 14040);
INSERT INTO monikielinenteksti (id, version) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0);
INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 1, '1995-01-01', 14040, (SELECT MAX(id) FROM monikielinenteksti));
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen aikuislukio', 'fi');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen aikuislukio', 'sv');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen aikuislukio', 'en');

