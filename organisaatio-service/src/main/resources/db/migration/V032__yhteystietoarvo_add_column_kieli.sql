ALTER TABLE yhteystietoarvo ADD COLUMN kieli VARCHAR(255);
UPDATE yhteystietoarvo SET kieli = 'kieli_fi#1';
