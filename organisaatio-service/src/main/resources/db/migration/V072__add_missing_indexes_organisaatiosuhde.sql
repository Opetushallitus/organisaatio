--
-- Lisätään puutuvia indeksejä
--

-- Taulu: organisaatiosuhde
CREATE INDEX organisaatiosuhde_suhdetyyppi_idx ON organisaatiosuhde (suhdetyyppi);
CREATE INDEX organisaatiosuhde_alkupvm_idx ON organisaatiosuhde (alkupvm);
CREATE INDEX organisaatiosuhde_loppupvm_idx ON organisaatiosuhde (loppupvm);

