ALTER TABLE organisaatio ADD if not exists piilotettu BOOLEAN;
UPDATE organisaatio SET piilotettu=false;
ALTER TABLE organisaatio ALTER COLUMN piilotettu SET not null;
UPDATE organisaatio SET piilotettu=true
From varhaiskasvatuksen_toimipaikka_tiedot vtt where vtt.id=varhaiskasvatuksen_toimipaikka_tiedot_id and vtt.toimintamuoto in ('vardatoimintamuoto_tm02', 'vardatoimintamuoto_tm03');
Alter TABLE varhaiskasvatuksen_toimipaikka_tiedot DROP COLUMN piilotettu;