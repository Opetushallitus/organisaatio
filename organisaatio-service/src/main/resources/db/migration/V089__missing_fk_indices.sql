-- Puuttuvat fk indeksit
create index organisaatio_id_idx on organisaatio_lisatieto (organisaatio_id);
create index organisaatio_id_idx on yhteystietoarvo (organisaatio_id);
create index organisaatiometadata_id_idx on organisaatiometadata_yhteystieto (organisaatiometadata_id);
create index organisaatiometadata_id_idx on organisaatiometadata_yhteystieto (organisaatiometadata_id);
create index hakutoimistonimi_id_idx on organisaatiometadata (hakutoimistonimi_id);
create index kuva_id_idx on organisaatiometadata (kuva_id);
create index hakutoimistoectsemailmkt_idx on organisaatiometadata (hakutoimistoectsemailmkt);
create index hakutoimistoectsnimimkt_idx on organisaatiometadata (hakutoimistoectsnimimkt);
create index hakutoimistoectspuhelinmkt_idx on organisaatiometadata (hakutoimistoectspuhelinmkt);
create index hakutoimistoectstehtavanimikemkt_idx on organisaatiometadata (hakutoimistoectstehtavanimikemkt);
create index varhaiskasvatuksen_toimipaikka_tiedot_id_idx on organisaatio (varhaiskasvatuksen_toimipaikka_tiedot_id);
create index kuvaus_mkt_idx on organisaatio (kuvaus_mkt);
create index metadata_id_idx on organisaatio (metadata_id);
create index nimi_mkt_idx on organisaatio (nimi_mkt);
create index name_id_idx on namedmonikielinenteksti (name_id);
create index value_id_idx on namedmonikielinenteksti (value_id);
create index org_nimi_mkt_idx on organisaatio_nimi (nimi_mkt);

-- Nimi on aina pakollinen tieto
alter table organisaatio alter column nimi_mkt set not null;
