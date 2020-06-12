ALTER TABLE yhteystietoarvo ALTER COLUMN kieli SET not null;
ALTER TABLE yhteystietoarvo DROP CONSTRAINT yhteystietoarvo_kentta_id_organisaatio_id_key;
ALTER TABLE yhteystietoarvo ADD CONSTRAINT yhteystietoarvo_kentta_id_organisaatio_id_key UNIQUE(kentta_id, organisaatio_id, kieli);