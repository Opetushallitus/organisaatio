create sequence hibernate_sequence;

create table monikielinenteksti
(
    id      bigint not null
        primary key,
    version bigint not null
);

create table binarydata
(
    id       bigint not null
        primary key,
    version  bigint not null,
    data     oid,
    filename varchar(255),
    key      varchar(255),
    mimetype varchar(255),
    name_id  bigint
        constraint fk4250636be5979fae
            references monikielinenteksti
);

create table monikielinenteksti_values
(
    id    bigint                                     not null
        constraint fk6f042a037ef4c57a
            references monikielinenteksti,
    value varchar(16384),
    key   varchar(255)                               not null,
    index varchar(15) default '0'::character varying not null,
    primary key (id, key, index)
);

create table namedmonikielinenteksti
(
    id       bigint not null
        primary key,
    version  bigint not null,
    key      varchar(255),
    name_id  bigint
        constraint fkd3eb7957e5979fae
            references monikielinenteksti,
    value_id bigint
        constraint fkd3eb79572ae75708
            references monikielinenteksti
);

create index namedmonikielinenteksti_name_id_idx
    on namedmonikielinenteksti (name_id);

create index namedmonikielinenteksti_value_id_idx
    on namedmonikielinenteksti (value_id);

create table organisaatiometadata
(
    id                               bigint    not null
        primary key,
    version                          bigint    not null,
    koodi                            varchar(255),
    luontipvm                        timestamp not null,
    muokkauspvm                      timestamp not null,
    nimi_id                          bigint
        constraint fk1c883eeef3403622
            references monikielinenteksti,
    hakutoimistonimi_id              bigint
        constraint fk1c883eee93d0549b
            references monikielinenteksti,
    kuva_id                          bigint
        constraint fk1c883eeef27eb12
            references binarydata,
    hakutoimistoectsemailmkt         bigint,
    hakutoimistoectsnimimkt          bigint,
    hakutoimistoectspuhelinmkt       bigint,
    hakutoimistoectstehtavanimikemkt bigint
);

create index hakutoimistonimi_id_idx
    on organisaatiometadata (hakutoimistonimi_id);

create index organisaatiometadata_kuva_id_idx
    on organisaatiometadata (kuva_id);

create index organisaatiometadata_hakutoimistoectsemailmkt_idx
    on organisaatiometadata (hakutoimistoectsemailmkt);

create index organisaatiometadata_hakutoimistoectsnimimkt_idx
    on organisaatiometadata (hakutoimistoectsnimimkt);

create index organisaatiometadata_hakutoimistoectspuhelinmkt_idx
    on organisaatiometadata (hakutoimistoectspuhelinmkt);

create index organisaatiometadata_hakutoimistoectstehtavanimikemkt_idx
    on organisaatiometadata (hakutoimistoectstehtavanimikemkt);

create table organisaatiometadata_namedmonikielinenteksti
(
    organisaatiometadata_id bigint not null
        constraint fkf4955306f4108c7c
            references organisaatiometadata,
    values_id               bigint not null
        unique
        constraint fkf495530635feb64d
            references namedmonikielinenteksti,
    primary key (organisaatiometadata_id, values_id)
);

create table yhteystietojentyyppi
(
    id       bigint       not null
        primary key,
    version  bigint       not null,
    oid      varchar(255) not null,
    nimi_mkt bigint
        constraint fk3f3c38979419034d
            references monikielinenteksti
);

create table yhteystietoelementti
(
    id                      bigint       not null
        primary key,
    version                 bigint       not null,
    kaytossa                boolean      not null,
    nimi                    varchar(100) not null,
    nimisv                  varchar(255),
    oid                     varchar(255) not null,
    pakollinen              boolean      not null,
    tyyppi                  varchar(255) not null,
    yhteystietojentyyppi_id bigint       not null
        constraint fkf5f2b32035a11c3b
            references yhteystietojentyyppi,
    nimien                  varchar(255),
    unique (yhteystietojentyyppi_id, nimi)
);

create table yhteystietojentyyppi_oppilaitostyypit
(
    yhteystietojentyyppi_id bigint not null
        constraint fk11d1b24d35a11c3b
            references yhteystietojentyyppi,
    oppilaitos_tyyppi       varchar(255)
);

create table yhteystietojentyyppi_organisaatiotyypit
(
    yhteystietojentyyppi_id bigint not null
        constraint fkae6e4e8e35a11c3b
            references yhteystietojentyyppi,
    organisaatio_tyyppi     varchar(255)
);

create table ytjpaivitysloki
(
    id                   bigint       not null
        primary key,
    version              bigint       not null,
    paivitysaika         timestamp    not null,
    paivitetyt_lkm       bigint,
    paivitys_tila        varchar(255) not null,
    paivitys_tila_selite varchar(255)
);

create index ytjpaivitysloki_paivitysaika_idx
    on ytjpaivitysloki (paivitysaika);

create table ytjvirhe
(
    id                 bigint       not null
        primary key,
    version            bigint       not null,
    oid                varchar(255) not null,
    orgnimi            varchar(255),
    ytjpaivitysloki_id bigint       not null
        constraint ytjvirhe_ytjpaivitys
            references ytjpaivitysloki,
    virhekohde         varchar(255),
    virheviesti        varchar(255)
);

create index ytjvirhe_oid_idx
    on ytjvirhe (oid);

create table lisatietotyyppi
(
    id      bigint not null
        primary key,
    version bigint not null,
    nimi    varchar(255)
        unique
);

create table rajoite
(
    rajoitetyyppi      varchar(31) not null,
    id                 bigint      not null
        primary key,
    version            bigint      not null,
    arvo               varchar(255),
    lisatietotyyppi_id bigint
        references lisatietotyyppi
);

create table spring_session
(
    primary_id            char(36) not null
        constraint spring_session_pk
            primary key,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100)
);

create unique index spring_session_ix1
    on spring_session (session_id);

create index spring_session_ix2
    on spring_session (expiry_time);

create index spring_session_ix3
    on spring_session (principal_name);

create table spring_session_attributes
(
    session_primary_id char(36)     not null
        constraint spring_session_attributes_fk
            references spring_session
            on delete cascade,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint spring_session_attributes_pk
        primary key (session_primary_id, attribute_name)
);

create table scheduled_tasks
(
    task_name            text                     not null,
    task_instance        text                     not null,
    task_data            bytea,
    execution_time       timestamp with time zone not null,
    picked               boolean                  not null,
    picked_by            text,
    last_success         timestamp with time zone,
    last_failure         timestamp with time zone,
    last_heartbeat       timestamp with time zone,
    version              bigint                   not null,
    consecutive_failures integer,
    primary key (task_name, task_instance)
);

create table varhaiskasvatuksen_toimipaikka_tiedot
(
    id                            bigint       not null
        primary key,
    version                       bigint       not null,
    toimintamuoto                 varchar(255) not null,
    kasvatusopillinen_jarjestelma varchar(255) not null,
    paikkojen_lukumaara           bigint       not null
);

create table organisaatio
(
    id                                       bigint  not null
        primary key,
    version                                  bigint  not null,
    alkupvm                                  date,
    domainnimi                               varchar(255),
    kotipaikka                               varchar(255),
    lakkautuspvm                             date,
    maa                                      varchar(255),
    nimihaku                                 varchar(256000),
    oid                                      varchar(255)
        unique,
    oppilaitoskoodi                          varchar(255)
        unique,
    oppilaitostyyppi                         varchar(255),
    organisaatiopoistettu                    boolean,
    yritysmuoto                              varchar(255),
    ytjpaivityspvm                           date,
    ytunnus                                  varchar(255),
    kuvaus_mkt                               bigint
        constraint fk4415da7f181d43a9
            references monikielinenteksti,
    metadata_id                              bigint
        constraint fk4415da7fc91e743b
            references organisaatiometadata,
    nimi_mkt                                 bigint  not null
        constraint fk4415da7f9419034d
            references monikielinenteksti,
    parentidpath                             varchar(512),
    opetuspisteenjarjnro                     varchar(255),
    yhteishaunkoulukoodi                     varchar(255),
    organisaatiotyypitstr                    varchar(255),
    virastotunnus                            varchar(255),
    tuontipvm                                timestamp,
    toimipistekoodi                          varchar(32),
    paivityspvm                              timestamp,
    paivittaja                               varchar(255),
    ytjkieli                                 varchar(255),
    tarkastuspvm                             timestamp,
    varhaiskasvatuksen_toimipaikka_tiedot_id bigint
        references varhaiskasvatuksen_toimipaikka_tiedot,
    piilotettu                               boolean not null,
    unique (ytunnus, organisaatiopoistettu)
);

create index organisaatio_alkupvm_idx
    on organisaatio (alkupvm);

create index organisaatio_lakkautuspvm_idx
    on organisaatio (lakkautuspvm);

create index organisaatio_oid_idx
    on organisaatio (oid);

create index organisaatio_paivityspvm_idx
    on organisaatio (paivityspvm);

create index organisaatio_toimipistekoodi_idx
    on organisaatio (toimipistekoodi);

create index organisaatio_varhaiskasvatuksen_toimipaikka_tiedot_id_idx
    on organisaatio (varhaiskasvatuksen_toimipaikka_tiedot_id);

create index organisaatio_kuvaus_mkt_idx
    on organisaatio (kuvaus_mkt);

create index organisaatio_metadata_id_idx
    on organisaatio (metadata_id);

create index organisaatio_nimi_mkt_idx
    on organisaatio (nimi_mkt);

create table organisaatio_kayttoryhmat
(
    organisaatio_id bigint not null
        constraint fk5a76e4323f4b641b
            references organisaatio,
    kayttoryhmat    varchar(255)
);

create index organisaatio_kayttoryhmat_organisaatio_id_idx
    on organisaatio_kayttoryhmat (organisaatio_id);

create table organisaatio_nimi
(
    id              bigint not null
        primary key,
    version         bigint not null,
    alkupvm         date   not null,
    organisaatio_id bigint not null
        constraint fkd68ae7a3f4b641b
            references organisaatio,
    nimi_mkt        bigint
        constraint fk4415da7f181d43a9
            references monikielinenteksti,
    paivittaja      varchar(255)
);

create index organisaatio_nimi_alkupvm_idx
    on organisaatio_nimi (alkupvm);

create index organisaatio_nimi_organisaatio_id_alkupvm_idx
    on organisaatio_nimi (organisaatio_id, alkupvm);

create index organisaatio_nimi_organisaatio_id_idx
    on organisaatio_nimi (organisaatio_id);

create index organisaatio_nimi_org_nimi_mkt_idx
    on organisaatio_nimi (nimi_mkt);

create table organisaatio_ryhmatyypit
(
    organisaatio_id bigint not null
        constraint fk0f14b15f3f4b641b
            references organisaatio,
    ryhmatyypit     varchar(255)
);

create index organisaatio_ryhmatyypit_organisaatio_id_idx
    on organisaatio_ryhmatyypit (organisaatio_id);

create table organisaatio_tyypit
(
    organisaatio_id bigint not null
        constraint fk56c654673f4b641b
            references organisaatio,
    tyypit          varchar(255),
    unique (organisaatio_id, tyypit)
);

create index organisaatio_tyypit_organisaatio_id_idx
    on organisaatio_tyypit (organisaatio_id);

create table organisaatio_vuosiluokat
(
    organisaatio_id bigint not null
        constraint fkba6a319e3f4b641b
            references organisaatio,
    vuosiluokat     varchar(255)
);

create index organisaatio_vuosiluokat_organisaatio_id_idx
    on organisaatio_vuosiluokat (organisaatio_id);

create table organisaatiosuhde
(
    id                   bigint       not null
        primary key,
    version              bigint       not null,
    suhdetyyppi          varchar(255) not null,
    child_id             bigint       not null
        constraint fkf600ee483ba9203e
            references organisaatio,
    parent_id            bigint       not null
        constraint fkf600ee485425b9f0
            references organisaatio,
    opetuspisteenjarjnro varchar(255),
    paivityspvm          date         default ('now'::text)::date,
    paivittaja           varchar(255) default "current_user"(),
    alkupvm              date,
    loppupvm             date
);

create index organisaatiosuhde_alkupvm_idx
    on organisaatiosuhde (alkupvm);

create index organisaatiosuhde_child_id_idx
    on organisaatiosuhde (child_id);

create index organisaatiosuhde_loppupvm_idx
    on organisaatiosuhde (loppupvm);

create index organisaatiosuhde_parent_id_idx
    on organisaatiosuhde (parent_id);

create index organisaatiosuhde_suhdetyyppi_idx
    on organisaatiosuhde (suhdetyyppi);

create table yhteystieto
(
    dtype            varchar(31)  not null,
    id               bigint       not null
        primary key,
    version          bigint       not null,
    yhteystietooid   varchar(255) not null,
    coordinatetype   varchar(255),
    extrarivi        varchar(255),
    lat              double precision,
    lng              double precision,
    maa              varchar(255),
    osavaltio        varchar(255),
    osoite           varchar(100),
    osoitetyyppi     varchar(255),
    postinumero      varchar(255),
    postitoimipaikka varchar(100),
    ytjpaivityspvm   date,
    puhelinnumero    varchar(255),
    tyyppi           varchar(255),
    email            varchar(255),
    wwwosoite        varchar(255),
    organisaatio_id  bigint
        constraint fkf7d456513f4b641b
            references organisaatio,
    kieli            varchar(255)
);

create table organisaatiometadata_yhteystieto
(
    organisaatiometadata_id bigint not null
        constraint fkdcabfe80f4108c7c
            references organisaatiometadata,
    yhteystiedot_id         bigint not null
        unique
        constraint fkdcabfe80f46f0057
            references yhteystieto
);

create index organisaatiometadata_id_idx
    on organisaatiometadata_yhteystieto (organisaatiometadata_id);

create index yhteystieto_organisaatio_id_idx
    on yhteystieto (organisaatio_id);

create table yhteystietoarvo
(
    id                 bigint       not null
        primary key,
    version            bigint       not null,
    arvotext           varchar(100),
    yhteystietoarvooid varchar(255) not null,
    arvoyhteystieto_id bigint
        constraint fkae4e0c9be6d89263
            references yhteystieto,
    kentta_id          bigint       not null
        constraint fkae4e0c9bd878984e
            references yhteystietoelementti,
    organisaatio_id    bigint       not null
        constraint fkae4e0c9b3f4b641b
            references organisaatio,
    kieli              varchar(255) not null,
    constraint yhteystietoarvo_kentta_id_organisaatio_id_key
        unique (kentta_id, organisaatio_id, kieli)
);

create index yhteystietoarvo_organisaatio_id_idx
    on yhteystietoarvo (organisaatio_id);

create table organisaatio_lisatieto
(
    id                 bigint not null
        primary key,
    version            bigint not null,
    organisaatio_id    bigint
        references organisaatio,
    lisatietotyyppi_id bigint
        references lisatietotyyppi
);

create index organisaatio_lisatieto_organisaatio_id_idx
    on organisaatio_lisatieto (organisaatio_id);

create table organisaatio_kielet
(
    organisaatio_id bigint       not null
        references organisaatio,
    kielet          varchar(255) not null,
    primary key (organisaatio_id, kielet)
);

create table varhaiskasvatuksen_kielipainotus
(
    id                                       bigint       not null
        primary key,
    version                                  bigint       not null,
    varhaiskasvatuksen_toimipaikka_tiedot_id bigint       not null
        constraint varhaiskasvatuksen_kielipaino_varhaiskasvatuksen_toimipaik_fkey
            references varhaiskasvatuksen_toimipaikka_tiedot,
    kielipainotus                            varchar(255) not null,
    alkupvm                                  timestamp    not null,
    loppupvm                                 timestamp
);

create table varhaiskasvatuksen_toiminnallinenpainotus
(
    id                                       bigint       not null
        primary key,
    version                                  bigint       not null,
    varhaiskasvatuksen_toimipaikka_tiedot_id bigint       not null
        constraint varhaiskasvatuksen_toiminnall_varhaiskasvatuksen_toimipaik_fkey
            references varhaiskasvatuksen_toimipaikka_tiedot,
    toiminnallinenpainotus                   varchar(255) not null,
    alkupvm                                  timestamp    not null,
    loppupvm                                 timestamp
);

create table varhaiskasvatuksen_jarjestamismuoto
(
    varhaiskasvatuksen_toimipaikka_tiedot_id bigint       not null
        constraint varhaiskasvatuksen_toimintamu_varhaiskasvatuksen_toimipaik_fkey
            references varhaiskasvatuksen_toimipaikka_tiedot,
    toimintamuoto                            varchar(255) not null
);

create table organisaatio_sahkoposti
(
    id                  bigint       not null
        primary key,
    version             bigint       not null,
    aikaleima           timestamp    not null,
    tyyppi              varchar(255) not null,
    viestintapalvelu_id varchar(255),
    organisaatio_id     bigint       not null
        references organisaatio
);

create table organisaatio_muut_kotipaikat_uris
(
    organisaatio_id bigint       not null
        constraint fk4nfmp4pb08x2vntptw88pgcu7
            references organisaatio,
    muut_kotipaikat varchar(255) not null,
    primary key (organisaatio_id, muut_kotipaikat)
);

create table organisaatio_muut_oppilaitostyypit
(
    organisaatio_id  bigint       not null
        references organisaatio,
    oppilaitostyyppi varchar(255) not null,
    primary key (organisaatio_id, oppilaitostyyppi)
);

create table organisaatio_parent_oids
(
    organisaatio_id bigint       not null
        references organisaatio
            on delete cascade,
    parent_position integer      not null,
    parent_oid      varchar(255) not null,
    unique (organisaatio_id, parent_oid),
    unique (organisaatio_id, parent_position)
);

create index organisaatio_parent_oids_fk_idx
    on organisaatio_parent_oids (organisaatio_id);

create index organisaatio_parent_oids_parent_idx
    on organisaatio_parent_oids (parent_oid);

insert into monikielinenteksti (id, version) values (0, 1);
insert into monikielinenteksti_values (id, value, key) values (0, 'Opetushallitus', 'fi');
insert into monikielinenteksti_values (id, value, key) values (0, 'Utbildningsstyrelsen', 'sv');
insert into monikielinenteksti_values (id, value, key) values (0, 'Finnish National Agency for Education', 'en');
insert into organisaatio (id, version, alkuPvm, maa, nimihaku, oid, organisaatioPoistettu, nimi_mkt, organisaatiotyypitstr, paivityspvm, piilotettu) values
    (0, 0, '1970-01-01', 'maatjavaltiot1_fin', 'Finnish National Agency for Education,Opetushallitus,Utbildningsstyrelsen', '1.2.246.562.10.00000000001', false, 0, 'Muu organisaatio|', '1992-01-01', false);
insert into organisaatio_tyypit (organisaatio_id,tyypit) values (0, 'organisaatiotyyppi_05');

-- yhteystietojen tyypit?