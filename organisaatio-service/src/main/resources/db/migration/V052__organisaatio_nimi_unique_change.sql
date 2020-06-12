--
-- Muutetaan organisaatio_nimi uniikkimääritys
--

ALTER TABLE organisaatio_nimi DROP CONSTRAINT organisaatio_nimi_organisaatio_id_alkupvm_nimi_mkt_key;
ALTER TABLE organisaatio_nimi ADD UNIQUE (organisaatio_id, alkuPvm);

