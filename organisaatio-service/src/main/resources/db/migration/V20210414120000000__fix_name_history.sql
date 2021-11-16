--
-- delete erroneous entry from 1.2.246.562.10.43886782945
--
ALTER TABLE organisaatio_nimi
    DROP CONSTRAINT FK4415DA7F181D43A9;

delete from monikielinenteksti_values where id = (select nimi_mkt from organisaatio_nimi where organisaatio_id = (select id from organisaatio where oid = '1.2.246.562.10.43886782945') and alkupvm = '1995-01-01');
delete from monikielinenteksti where id = (select nimi_mkt from organisaatio_nimi where organisaatio_id = (select id from organisaatio where oid = '1.2.246.562.10.43886782945') and alkupvm = '1995-01-01');
delete from organisaatio_nimi where organisaatio_id = (select id from organisaatio where oid = '1.2.246.562.10.43886782945') and alkupvm = '1995-01-01';

ALTER TABLE organisaatio_nimi
    ADD CONSTRAINT FK4415DA7F181D43A9
        FOREIGN KEY (nimi_mkt)
            REFERENCES MonikielinenTeksti;

--
-- Fix entry name for 1.2.246.562.10.43886782945
--
UPDATE monikielinenteksti_values set value = 'Riihimäen lukio' where id = (select nimi_mkt from organisaatio_nimi where alkupvm = '1992-01-01' and organisaatio_id = (select id from organisaatio where oid = '1.2.246.562.10.43886782945'));

--
-- Insert new entry for 1.2.246.562.10.60636365854
--
INSERT INTO organisaatio (id, version, oid, nimi_mkt, piilotettu) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0, '1.2.246.562.10.60636365854', 0, false) ON CONFLICT DO NOTHING;
update organisaatio_nimi set alkupvm = '1995-01-01' where organisaatio_id = (select id from organisaatio where oid = '1.2.246.562.10.60636365854');
INSERT INTO monikielinenteksti (id, version) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 0);
INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt) VALUES ((SELECT NEXTVAL('hibernate_sequence')), 1, '1992-01-01', (select id from organisaatio where oid = '1.2.246.562.10.60636365854'), (SELECT MAX(id) FROM monikielinenteksti));
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen iltalukio', 'fi');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen iltalukio', 'sv');
INSERT INTO monikielinenteksti_values (id, value, key) VALUES ((SELECT MAX(id) FROM monikielinenteksti), 'Riihimäen iltalukio', 'en');
