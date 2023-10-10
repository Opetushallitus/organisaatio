
--
-- OVT-4954 Add "Luontainen tunniste" / "Natural key" to Organisations
--
ALTER TABLE organisaatio ADD COLUMN toimipisteKoodi varchar(32);

--
-- Field is used to search entries
--
CREATE INDEX ON organisaatio (toimipisteKoodi);
