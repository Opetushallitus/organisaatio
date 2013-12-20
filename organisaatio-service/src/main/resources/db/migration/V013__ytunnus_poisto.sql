-- poistettu -> nullable
ALTER TABLE organisaatio ALTER organisaatiopoistettu DROP not null;

-- true -> null
UPDATE organisaatio SET organisaatiopoistettu = null WHERE organisaatiopoistettu = true;

-- poista ytunnus-uniikkius
ALTER TABLE organisaatio DROP CONSTRAINT organisaatio_ytunnus_key;

-- lisää ytunnus+poistettu -uniikkius
ALTER TABLE organisaatio ADD CONSTRAINT organisaatio_ytunnus_organisaatiopoistettu_key unique(ytunnus, organisaatiopoistettu);