UPDATE monikielinenteksti_values SET index=0 WHERE index IS NULL;
ALTER TABLE monikielinenteksti_values ALTER COLUMN index SET NOT NULL;
ALTER TABLE monikielinenteksti_values ALTER COLUMN index SET DEFAULT '0';

ALTER TABLE monikielinenteksti_values DROP CONSTRAINT monikielinenteksti_values_pkey;
ALTER TABLE monikielinenteksti_values ADD CONSTRAINT monikielinenteksti_values_pkey PRIMARY KEY (id, key, index);
