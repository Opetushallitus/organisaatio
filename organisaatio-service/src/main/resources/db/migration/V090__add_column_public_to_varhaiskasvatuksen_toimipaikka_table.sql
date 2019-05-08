ALTER TABLE varhaiskasvatuksen_toimipaikka_tiedot ADD if not exists julkinen BOOLEAN;
UPDATE varhaiskasvatuksen_toimipaikka_tiedot SET julkinen=false;
ALTER TABLE varhaiskasvatuksen_toimipaikka_tiedot ALTER COLUMN julkinen SET not null;

