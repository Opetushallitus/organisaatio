alter table varhaiskasvatuksen_toimipaikka_tiedot drop column if exists toiminnallinen_painotus;

alter table varhaiskasvatuksen_kielipainotus alter column loppupvm drop not null;

alter table varhaiskasvatuksen_toiminnallinenpainotus alter column loppupvm drop not null;

alter table varhaiskasvatuksen_toimintamuoto rename to varhaiskasvatuksen_jarjestamismuoto;

alter table varhaiskasvatuksen_toimipaikka_tiedot rename column jarjestamismuoto to toimintamuoto;