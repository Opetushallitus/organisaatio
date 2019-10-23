ALTER TABLE organisaatio DROP CONSTRAINT organisaatio_pkey;
ALTER TABLE organisaatio ADD PRIMARY KEY (rekisterointi_id);
