--
-- Siirretään organisaatioliitos taulusta liitokset organisaatio suhde tauluun
-- Poistetaan organisaatioliitos taulu turhana
--

INSERT INTO organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm)
SELECT nextval('public.hibernate_sequence'), version, 'LIITOS', organisaatio_id, kohde_id, alkupvm
FROM organisaatioliitos;

DROP TABLE organisaatioliitos;