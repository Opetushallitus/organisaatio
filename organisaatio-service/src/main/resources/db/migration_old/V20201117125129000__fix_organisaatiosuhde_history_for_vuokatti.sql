--
-- OPH-652
-- Vuokatin urheiluopisto on virheellisesti merkitty Kajaanin alaisuuteen.
-- Postetaan virheellinen relaatio ja korjataan historia- sekä hierarkiatietoja
--
-- ID/OID
-- Vuokatti (toimipiste): 44438 /1.2.246.562.10.39367508924
-- Vuokatti (oppilaitos): 24986 / 1.2.246.562.10.36322890983
-- Kajaani: 4061 / 1.2.246.562.10.67019405611
-- Vuokatin säätiö: 2544 / 1.2.246.562.10.96735967042
--

-- Poista Kajaani->Vuokatti relaatio
DELETE FROM organisaatiosuhde WHERE child_id = 24986 AND parent_id = 4061;

-- Muokkaa historiaa (palauta) Vuokatti -> Vuokatin säätiö relaatio
UPDATE organisaatiosuhde SET loppupvm = NULL WHERE child_id = 24986 AND parent_id = 2544;

-- Korjaa hierarkia: Vuokatti (Oppilaitos)
UPDATE organisaatio SET parentidpath = '|0|2544|' WHERE id = 24986;
UPDATE organisaatio_parent_oids SET parent_oid = '1.2.246.562.10.96735967042' WHERE organisaatio_id = 24986 AND parent_oid = '1.2.246.562.10.67019405611';

-- Korjaa hierarkia: Vuokatti (Toimipiste)
UPDATE organisaatio SET parentidpath = '|0|2544|24986|' WHERE id = 44438;
UPDATE organisaatio_parent_oids SET parent_oid = '1.2.246.562.10.96735967042' WHERE organisaatio_id = 44438 AND parent_oid = '1.2.246.562.10.67019405611';
