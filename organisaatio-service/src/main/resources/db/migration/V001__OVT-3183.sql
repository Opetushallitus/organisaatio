ALTER TABLE OrganisaatioMetaData ADD COLUMN hakutoimistoEctsEmail varchar(255);
ALTER TABLE OrganisaatioMetaData ADD COLUMN hakutoimistoEctsNimi varchar(255);
ALTER TABLE OrganisaatioMetaData ADD COLUMN hakutoimistoEctsPuhelin varchar(255);
ALTER TABLE OrganisaatioMetaData ADD COLUMN hakutoimistoEctsTehtavanimike varchar(255);
ALTER TABLE OrganisaatioMetaData ADD COLUMN hakutoimistoNimi_id int8;
ALTER TABLE OrganisaatioMetaData ADD COLUMN kuva_id int8;

CREATE TABLE OrganisaatioMetaData_Yhteystieto ( 
    OrganisaatioMetaData_id int8 not null,
    yhteystiedot_id int8 not null,
    unique (yhteystiedot_id)
);

ALTER TABLE OrganisaatioMetaData
    ADD CONSTRAINT FK1C883EEE93D0549B
    foreign key (hakutoimistoNimi_id)
    references MonikielinenTeksti;

ALTER TABLE OrganisaatioMetaData
    ADD CONSTRAINT FK1C883EEEF27EB12
    foreign key (kuva_id)
    references BinaryData;

ALTER TABLE OrganisaatioMetaData_Yhteystieto
    ADD CONSTRAINT FKDCABFE80F4108C7C
    foreign key (OrganisaatioMetaData_id)
    references OrganisaatioMetaData;

ALTER TABLE OrganisaatioMetaData_Yhteystieto
    ADD CONSTRAINT FKDCABFE80F46F0057
    foreign key (yhteystiedot_id)
    references Yhteystieto;
