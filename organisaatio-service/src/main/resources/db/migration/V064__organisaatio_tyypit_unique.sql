--
-- Estetään duplikaattirivit taulusta organisaatio_tyypit
--

ALTER TABLE organisaatio_tyypit ADD UNIQUE (organisaatio_id, tyypit);
