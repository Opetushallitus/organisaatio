--
-- Poistetaan OrganisaatioMetaData taulusta columnit, joilla on MKT vastine
--

ALTER TABLE OrganisaatioMetaData DROP COLUMN hakutoimistoEctsEmail;
ALTER TABLE OrganisaatioMetaData DROP COLUMN hakutoimistoEctsNimi;
ALTER TABLE OrganisaatioMetaData DROP COLUMN hakutoimistoEctsPuhelin;
ALTER TABLE OrganisaatioMetaData DROP COLUMN hakutoimistoEctsTehtavanimike;

