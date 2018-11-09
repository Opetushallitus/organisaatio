-- Puuttuvat fk indeksit
create index organisaatio_lisatieto_organisaatio_id_idx on organisaatio_lisatieto (organisaatio_id);
create index yhteystietoarvo_organisaatio_id_idx on yhteystietoarvo (organisaatio_id);
create index organisaatiometadata_id_idx on organisaatiometadata_yhteystieto (organisaatiometadata_id);
create index hakutoimistonimi_id_idx on organisaatiometadata (hakutoimistonimi_id);
create index organisaatiometadata_kuva_id_idx on organisaatiometadata (kuva_id);
create index organisaatiometadata_hakutoimistoectsemailmkt_idx on organisaatiometadata (hakutoimistoectsemailmkt);
create index organisaatiometadata_hakutoimistoectsnimimkt_idx on organisaatiometadata (hakutoimistoectsnimimkt);
create index organisaatiometadata_hakutoimistoectspuhelinmkt_idx on organisaatiometadata (hakutoimistoectspuhelinmkt);
create index organisaatiometadata_hakutoimistoectstehtavanimikemkt_idx on organisaatiometadata (hakutoimistoectstehtavanimikemkt);
create index organisaatio_varhaiskasvatuksen_toimipaikka_tiedot_id_idx on organisaatio (varhaiskasvatuksen_toimipaikka_tiedot_id);
create index organisaatio_kuvaus_mkt_idx on organisaatio (kuvaus_mkt);
create index organisaatio_metadata_id_idx on organisaatio (metadata_id);
create index organisaatio_nimi_mkt_idx on organisaatio (nimi_mkt);
create index namedmonikielinenteksti_name_id_idx on namedmonikielinenteksti (name_id);
create index namedmonikielinenteksti_value_id_idx on namedmonikielinenteksti (value_id);
create index organisaatio_nimi_org_nimi_mkt_idx on organisaatio_nimi (nimi_mkt);

-- Nimi on aina pakollinen tieto
alter table organisaatio alter column nimi_mkt set not null;
