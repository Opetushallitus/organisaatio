
    create table BinaryData (
        id int8 not null unique,
        version int8 not null,
        data oid,
        filename varchar(255),
        key varchar(255),
        mimeType varchar(255),
        name_id int8,
        primary key (id)
    );

    create table MonikielinenTeksti (
        id int8 not null unique,
        version int8 not null,
        primary key (id)
    );

    create table MonikielinenTeksti_values (
        id int8 not null,
        value varchar(4096),
        key varchar(255) not null,
        primary key (id, key)
    );

    create table NamedMonikielinenTeksti (
        id int8 not null unique,
        version int8 not null,
        key varchar(255),
        name_id int8,
        value_id int8,
        primary key (id)
    );

    create table Organisaatio (
        id int8 not null unique,
        version int8 not null,
        alkuPvm date,
        domainNimi varchar(255),
        kotipaikka varchar(255),
        kuvaus varchar(255),
        lakkautusPvm date,
        maa varchar(255),
        nimiLyhenne varchar(10),
        nimihaku varchar(255),
        oid varchar(255),
        oppilaitosKoodi varchar(255) unique,
        oppilaitosTyyppi varchar(255),
        organisaatioPoistettu bool not null,
        yritysmuoto varchar(255),
        ytjPaivitysPvm date,
        ytunnus varchar(255) unique,
        kuvaus_mkt int8,
        metadata_id int8,
        nimi_mkt int8,
        parent_id int8,
        primary key (id),
        unique (oid)
    );

    create table OrganisaatioMetaData (
        id int8 not null unique,
        version int8 not null,
        koodi varchar(255),
        luontiPvm timestamp not null,
        muokkausPvm timestamp not null,
        nimi_id int8,
        primary key (id)
    );

    create table OrganisaatioMetaData_NamedMonikielinenTeksti (
        OrganisaatioMetaData_id int8 not null,
        values_id int8 not null,
        primary key (OrganisaatioMetaData_id, values_id),
        unique (values_id)
    );

    create table Yhteystieto (
        DTYPE varchar(31) not null,
        id int8 not null unique,
        version int8 not null,
        yhteystietoOid varchar(255) not null,
        coordinateType varchar(255),
        extraRivi varchar(255),
        lat float8,
        lng float8,
        maa varchar(255),
        osavaltio varchar(255),
        osoite varchar(100),
        osoiteTyyppi varchar(255),
        postinumero varchar(255),
        postitoimipaikka varchar(100),
        ytjPaivitysPvm date,
        puhelinnumero varchar(255),
        tyyppi varchar(255),
        email varchar(255),
        wwwOsoite varchar(255),
        organisaatio_id int8,
        primary key (id)
    );

    create table YhteystietoArvo (
        id int8 not null unique,
        version int8 not null,
        arvoText varchar(100),
        yhteystietoArvoOid varchar(255) not null,
        arvoYhteystieto_id int8,
        kentta_id int8 not null,
        organisaatio_id int8 not null,
        primary key (id),
        unique (kentta_id, organisaatio_id)
    );

    create table YhteystietoElementti (
        id int8 not null unique,
        version int8 not null,
        kaytossa bool not null,
        nimi varchar(100) not null,
        nimiSv varchar(255),
        oid varchar(255) not null,
        pakollinen bool not null,
        tyyppi varchar(255) not null,
        yhteystietojenTyyppi_id int8 not null,
        primary key (id),
        unique (yhteystietojenTyyppi_id, nimi)
    );

    create table YhteystietojenTyyppi (
        id int8 not null unique,
        version int8 not null,
        oid varchar(255) not null,
        nimi_mkt int8,
        primary key (id)
    );

    create table organisaatio_kielet (
        organisaatio_id int8 not null,
        kielet varchar(255)
    );

    create table organisaatio_sopimuskunnat (
        organisaatio_id int8 not null,
        sopimusKunnat varchar(255)
    );

    create table organisaatio_tyypit (
        organisaatio_id int8 not null,
        tyypit varchar(255)
    );

    create table organisaatio_vuosiluokat (
        organisaatio_id int8 not null,
        vuosiluokat varchar(255)
    );

    create table yhteystietojenTyyppi_oppilaitosTyypit (
        yhteystietojenTyyppi_id int8 not null,
        oppilaitos_tyyppi varchar(255)
    );

    create table yhteystietojenTyyppi_organisaatioTyypit (
        yhteystietojenTyyppi_id int8 not null,
        organisaatio_tyyppi varchar(255)
    );

    alter table BinaryData 
        add constraint FK4250636BE5979FAE 
        foreign key (name_id) 
        references MonikielinenTeksti;

    alter table MonikielinenTeksti_values 
        add constraint FK6F042A037EF4C57A 
        foreign key (id) 
        references MonikielinenTeksti;

    alter table NamedMonikielinenTeksti 
        add constraint FKD3EB79572AE75708 
        foreign key (value_id) 
        references MonikielinenTeksti;

    alter table NamedMonikielinenTeksti 
        add constraint FKD3EB7957E5979FAE 
        foreign key (name_id) 
        references MonikielinenTeksti;

    alter table Organisaatio 
        add constraint FK4415DA7F181D43A9 
        foreign key (kuvaus_mkt) 
        references MonikielinenTeksti;

    alter table Organisaatio 
        add constraint FK4415DA7F9419034D 
        foreign key (nimi_mkt) 
        references MonikielinenTeksti;

    alter table Organisaatio 
        add constraint FK4415DA7F5425B9F0 
        foreign key (parent_id) 
        references Organisaatio;

    alter table Organisaatio 
        add constraint FK4415DA7FC91E743B 
        foreign key (metadata_id) 
        references OrganisaatioMetaData;

    alter table OrganisaatioMetaData 
        add constraint FK1C883EEEF3403622 
        foreign key (nimi_id) 
        references MonikielinenTeksti;

    alter table OrganisaatioMetaData_NamedMonikielinenTeksti 
        add constraint FKF4955306F4108C7C 
        foreign key (OrganisaatioMetaData_id) 
        references OrganisaatioMetaData;

    alter table OrganisaatioMetaData_NamedMonikielinenTeksti 
        add constraint FKF495530635FEB64D 
        foreign key (values_id) 
        references NamedMonikielinenTeksti;

    alter table Yhteystieto 
        add constraint FKF7D456513F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table YhteystietoArvo 
        add constraint FKAE4E0C9BD878984E 
        foreign key (kentta_id) 
        references YhteystietoElementti;

    alter table YhteystietoArvo 
        add constraint FKAE4E0C9B3F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table YhteystietoArvo 
        add constraint FKAE4E0C9BE6D89263 
        foreign key (arvoYhteystieto_id) 
        references Yhteystieto;

    alter table YhteystietoElementti 
        add constraint FKF5F2B32035A11C3B 
        foreign key (yhteystietojenTyyppi_id) 
        references YhteystietojenTyyppi;

    alter table YhteystietojenTyyppi 
        add constraint FK3F3C38979419034D 
        foreign key (nimi_mkt) 
        references MonikielinenTeksti;

    alter table organisaatio_kielet 
        add constraint FK468017D43F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table organisaatio_sopimuskunnat 
        add constraint FK60E6D6533F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table organisaatio_tyypit 
        add constraint FK56C654673F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table organisaatio_vuosiluokat 
        add constraint FKBA6A319E3F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table yhteystietojenTyyppi_oppilaitosTyypit 
        add constraint FK11D1B24D35A11C3B 
        foreign key (yhteystietojenTyyppi_id) 
        references YhteystietojenTyyppi;

    alter table yhteystietojenTyyppi_organisaatioTyypit 
        add constraint FKAE6E4E8E35A11C3B 
        foreign key (yhteystietojenTyyppi_id) 
        references YhteystietojenTyyppi;

    create sequence hibernate_sequence;

    insert into monikielinenteksti (id,version) values (0, 1);
    insert into monikielinenteksti_values (id,value,key) values (0, 'Opetushallitus', 'fi');
    insert into monikielinenteksti_values (id,value,key) values (0, 'Utbildningsstyrelsen', 'sv');
    insert into monikielinenteksti_values (id,value,key) values (0, 'The Finnish National Board of Education', 'en');
    insert into organisaatio (id,version,alkuPvm,nimihaku,oid,organisaatioPoistettu,nimi_mkt) values (0,0,'1970-01-01','oph,,','1.2.246.562.10.00000000001',FALSE,0);
    insert into organisaatio_tyypit (organisaatio_id,tyypit) values (0, 'Muu organisaatio');
