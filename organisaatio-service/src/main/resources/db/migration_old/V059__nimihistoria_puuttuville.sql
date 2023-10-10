--
-- Päivitetään nykyisten organisaatioiden nimet nimihistoriaan, niille organisaatioille, joilta puuttu nimihistoria
--

INSERT INTO organisaatio_nimi (id, version, organisaatio_id, alkuPvm, nimi_mkt)
SELECT nextval('public.hibernate_sequence'), 1, id, alkupvm, nimi_mkt
FROM organisaatio
WHERE id in (
--
-- Haetaan organisaatiot, joilta puuttuu nimihistoria
--
SELECT organisaatio.id
FROM organisaatio
full outer join organisaatio_nimi on organisaatio.id = organisaatio_nimi.organisaatio_id
WHERE organisaatio_nimi.organisaatio_id is null
and organisaatio.organisaatiotyypitstr not like '%Ryhma%');

