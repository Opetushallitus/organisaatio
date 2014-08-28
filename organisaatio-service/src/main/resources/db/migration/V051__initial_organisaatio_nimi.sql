--
-- Päivitetään nykyisten organisaatioiden nimet nimihistoriaan
--

INSERT INTO organisaatio_nimi (id, version, organisaatio_id, alkuPvm, nimi_mkt)
SELECT nextval('public.hibernate_sequence'), 1, id, alkupvm, nimi_mkt FROM organisaatio
WHERE organisaatio.organisaatiotyypitstr not like '%Ryhma%';
