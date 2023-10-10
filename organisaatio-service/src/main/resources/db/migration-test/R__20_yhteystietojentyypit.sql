INSERT INTO monikielinenteksti (id, version) VALUES (2087136, 0) ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti (id, version) VALUES (2118931, 0) ON CONFLICT DO NOTHING;

INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2087136, 'Kriisitiedotuksen sähköpostiosoite', 'en', '0') ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2087136, 'Kriisitiedotuksen sähköpostiosoite', 'fi', '0') ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2087136, 'Kriskommunikation epost', 'sv', '0') ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2118931, 'KOSKI-palvelun omien tietojen virheilmoituksen sähköpostiosoite', 'en', '0') ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2118931, 'KOSKI-palvelun omien tietojen virheilmoituksen sähköpostiosoite', 'fi', '0') ON CONFLICT DO NOTHING;
INSERT INTO monikielinenteksti_values (id, value, key, index) VALUES (2118931, 'E-postadress för felanmälan i egna uppgifter i KOSKI-tjänsten', 'sv', '0') ON CONFLICT DO NOTHING;

INSERT INTO yhteystietojentyyppi (id, version, oid, nimi_mkt) VALUES (1169841, 3, '1.2.246.562.5.31532764098', 2087136) ON CONFLICT DO NOTHING;
INSERT INTO yhteystietojentyyppi (id, version, oid, nimi_mkt) VALUES (2118919, 1, '1.2.246.562.5.79385887983', 2118931) ON CONFLICT DO NOTHING;

INSERT INTO public.yhteystietoelementti (id, version, kaytossa, nimi, nimisv, oid, pakollinen, tyyppi, yhteystietojentyyppi_id, nimien) VALUES (1169851, 0, true, 'Sähköpostiosoite', 'Epostadress', '1.2.246.562.5.30789631784', false, 'Email', 1169841, 'Email') ON CONFLICT DO NOTHING;
INSERT INTO public.yhteystietoelementti (id, version, kaytossa, nimi, nimisv, oid, pakollinen, tyyppi, yhteystietojentyyppi_id, nimien) VALUES (2118929, 0, true, 'Sähköpostiosoite', 'Epostadress', '1.2.246.562.5.57850489428', false, 'Email', 2118919, 'Email') ON CONFLICT DO NOTHING;


INSERT INTO yhteystietojentyyppi_organisaatiotyypit (yhteystietojentyyppi_id, organisaatio_tyyppi) VALUES (1169841, 'organisaatiotyyppi_01') ON CONFLICT DO NOTHING;
INSERT INTO yhteystietojentyyppi_organisaatiotyypit (yhteystietojentyyppi_id, organisaatio_tyyppi) VALUES (2118919, 'organisaatiotyyppi_02') ON CONFLICT DO NOTHING;
