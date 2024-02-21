ALTER TABLE osoitteet_haku_and_hakutulos ADD COLUMN kayttajat text;
ALTER TABLE osoitteet_haku_and_hakutulos ALTER COLUMN organisaatio_ids DROP NOT NULL;