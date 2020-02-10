-- varmistaa, että PK on olemassa, jotta päivitys 4.x:ään onnistuu
ALTER TABLE schema_version DROP CONSTRAINT IF EXISTS schema_version_pk;
ALTER TABLE schema_version ADD CONSTRAINT schema_version_pk PRIMARY KEY (installed_rank);
