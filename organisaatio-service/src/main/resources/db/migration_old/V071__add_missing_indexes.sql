--
-- Lisätään puutuvia indeksejä
--

-- Taulu organisaatio
CREATE INDEX organisaatio_paivityspvm_idx ON organisaatio (paivityspvm);
CREATE INDEX organisaatio_oid_idx ON organisaatio (oid);
CREATE INDEX organisaatio_alkupvm_idx ON organisaatio (alkupvm);
CREATE INDEX organisaatio_lakkautuspvm_idx ON organisaatio (lakkautuspvm);

-- Taulu: organisaatiosuhde
CREATE INDEX organisaatiosuhde_child_id_idx ON organisaatiosuhde (child_id);
CREATE INDEX organisaatiosuhde_parent_id_idx ON organisaatiosuhde (parent_id);

-- Taulu: organisaatio_kielet
CREATE INDEX organisaatio_kielet_organisaatio_id_idx ON organisaatio_kielet (organisaatio_id);

-- Taulu: "organisaatio_kayttoryhmat"
CREATE INDEX organisaatio_kayttoryhmat_organisaatio_id_idx ON organisaatio_kayttoryhmat (organisaatio_id);

-- Taulu: organisaatio_ryhmatyypit
CREATE INDEX organisaatio_ryhmatyypit_organisaatio_id_idx ON organisaatio_ryhmatyypit (organisaatio_id);

-- Taulu: organisaatio_vuosiluokat
CREATE INDEX organisaatio_vuosiluokat_organisaatio_id_idx ON organisaatio_vuosiluokat (organisaatio_id);

-- Taulu: organisaatio_tyypit
CREATE INDEX organisaatio_tyypit_organisaatio_id_idx ON organisaatio_tyypit (organisaatio_id);

-- Taulu: yhteystieto
CREATE INDEX yhteystieto_organisaatio_id_idx ON yhteystieto (organisaatio_id);
