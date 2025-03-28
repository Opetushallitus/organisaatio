CREATE OR REPLACE FUNCTION createOrganisaatio(oid1 varchar, nimi varchar, parentoid varchar, parentid bigint, parentidpath varchar, types varchar[], oppilaitostyyppi varchar, oppilaitoskoodi varchar, yritysmuoto varchar, vardatoimintamuoto varchar) returns RECORD
    language plpgsql
AS
$$
DECLARE
    ret RECORD;
begin
    WITH
        checker AS (select oid1 as newoid where not exists (select 1 from organisaatio where oid = oid1)),
        data    AS (SELECT 'test_data' as paivittaja, checker.newoid as oid, nimi from checker),
        mkt     AS (INSERT INTO monikielinenteksti (id,version) select (select max(id)+1 from monikielinenteksti),0 from data RETURNING id),
        mktval  AS (INSERT INTO monikielinenteksti_values (id, value, key) SELECT mkt.id,data.nimi,kieli.id FROM mkt join data on 1=1 join (values('fi'),('sv'),('en'))as kieli(id) on 1=1),
        vaka_tt AS (INSERT INTO varhaiskasvatuksen_toimipaikka_tiedot (id, version, toimintamuoto, kasvatusopillinen_jarjestelma, paikkojen_lukumaara)
                    SELECT (SELECT coalesce(max(id)+1,1) FROM varhaiskasvatuksen_toimipaikka_tiedot), 0, vardatoimintamuoto, 'vardakasvatusopillinenjarjestelma_kj98', 0 FROM checker WHERE vardatoimintamuoto IS NOT NULL RETURNING id),
        org     AS (INSERT into organisaatio (oid,id,version, nimi_mkt, piilotettu, organisaatiopoistettu, paivityspvm, paivittaja, alkupvm, kotipaikka, maa, parentidpath,oppilaitostyyppi, oppilaitoskoodi, nimihaku, yritysmuoto, varhaiskasvatuksen_toimipaikka_tiedot_id)
                    SELECT  data.oid, (select coalesce(max(id)+1,1) from organisaatio),0, mkt.id, false, false, CURRENT_TIMESTAMP, data.paivittaja, CURRENT_DATE, 'kunta_153', 'maatjavaltiot1_fin', parentidpath, oppilaitostyyppi, oppilaitoskoodi, createOrganisaatio.nimi, yritysmuoto, vaka_tt.id FROM mkt LEFT JOIN vaka_tt ON true join data on 1=1 RETURNING id, oid, organisaatio.parentidpath),
        orgnim  AS (INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt, paivittaja) SELECT (select coalesce(max(id)+1,1) from organisaatio_nimi),0, CURRENT_DATE, org.id, mkt.id, data.paivittaja  from org join mkt on 1=1 join data on 1=1 returning id),
        orgtyp  AS (INSERT INTO organisaatio_tyypit (organisaatio_id, tyypit) SELECT org.id, typ.id from org join unnest(types) as typ(id) on 1=1 returning organisaatio_tyypit.organisaatio_id, organisaatio_tyypit.tyypit),
        kieli1  AS (INSERT INTO organisaatio_kielet (organisaatio_id, kielet) SELECT id, 'oppilaitoksenopetuskieli_1' from org),
        parent  AS (INSERT INTO organisaatio_parent_oids (organisaatio_id, parent_position, parent_oid) SELECT org.id, 0, parentoid from org WHERE parentoid IS NOT NULL AND parentoid <> ''),
        suhde   AS (INSERT INTO organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) SELECT (select coalesce(max(id)+1,1) from organisaatiosuhde),0,'HISTORIA',org.id,parentid,CURRENT_DATE from org returning organisaatiosuhde.id),
        yhteys  AS (INSERT INTO yhteystieto (dtype, id, version,yhteystietooid, osoite, osoitetyyppi, postinumero, postitoimipaikka,email, organisaatio_id, kieli)
                    SELECT osoite.type, (select coalesce(max(id)+1,1) from yhteystieto)+osoite.number,0, osoite.oid,osoite.osoite, osoite.osoitetyyppi,osoite.postinumero, osoite.postitoimipaikka, osoite.email,org.id,osoite.kieli
                    FROM org,
                         (values ('Osoite',concat(oid1,'.1'),'Testikuja 5','posti','posti_00009','DIGITOINTI',null,'kieli_fi#1',1),
                                 ('Email' ,concat(oid1,'.2'),null,null,null,null,'testiposti@opetushallitus.fi','kieli_fi#1',2),
                                 ('Osoite',concat(oid1,'.3'),'Testikuja 5','kaynti','posti_00009','DIGITOINTI',null,'kieli_fi#1',3))
                             AS osoite(type, oid, osoite, osoitetyyppi, postinumero, postitoimipaikka, email, kieli, number))
    SELECT org.* into ret FROM org;
    return ret;
end;
$$;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000001','Mansikkalan testi kunta', '1.2.246.562.10.00000000001', 0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_09'], null, null, 'Kunta', null) as foo(id bigint,oid varchar, parentidpath varchar)),
     a as (select createOrganisaatio('1.2.246.562.99.00000000002','Mansikkalan testi peruskoulu',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_11#1', '30076', null, null) from parent where parent.oid is not null ),
     b as (select createOrganisaatio('1.2.246.562.99.00000000003','Mansikkalan testi lukio',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_15#1', '30077', null, null) from parent where parent.oid is not null)
select createOrganisaatio('1.2.246.562.99.00000000004','Ahomansikan päiväkoti',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, null) from parent where parent.oid is not null
union all select * from a union all select * from b;

insert into organisaatio_vuosiluokat select organisaatio.id, 'vuosiluokat_9#2' from organisaatio where oid = '1.2.246.562.99.00000000002';

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000005','Testi Koulutuskuntayhtymä Puolukka', '1.2.246.562.10.00000000001', 0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_09'], null, null, 'Kuntayhtymä', null) as foo(id bigint,oid varchar, parentidpath varchar)),
     a as (select createOrganisaatio('1.2.246.562.99.00000000006','Testi Ammattiopisto Puolukka',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_21#1', '30079', null, null) from parent where parent.oid is not null )
select createOrganisaatio('1.2.246.562.99.00000000007','Testi Puolukkalan liikuntaopisto',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_62#1', '30080', null, null) from parent where parent.oid is not null
union all select * from a ;

with parent as (select id,oid,parentidpath from organisaatio where oid = '1.2.246.562.99.00000000006')
select createOrganisaatio('1.2.246.562.99.00000000010','Ammattiopisto Puolukka, testi toimipiste',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_03'], null, null, null, null) from parent where parent.oid is not null;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000008','Mustikkalan testi yhdistys', '1.2.246.562.10.00000000001', 0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_05'], null, null, 'Säätiö', null) as foo(id bigint,oid varchar, parentidpath varchar))
select createOrganisaatio('1.2.246.562.99.00000000009','Mustikkalan testi opisto',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_63#1', '30081', null, null) from parent where parent.oid is not null;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000011','Varhaiskasvatuksen järjestäjä kunta testi', '1.2.246.562.10.00000000001', 0, '|0|', array['organisaatiotyyppi_07'], null, null, 'Kunta', null) as foo(id bigint,oid varchar, parentidpath varchar)),
    a as (select createOrganisaatio('1.2.246.562.99.00000000012','Metsämansikan testi päiväkoti',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm01') from parent where parent.oid is not null),
    b as (select createOrganisaatio('1.2.246.562.99.00000000013','Metsämuuraimen testi perhepäivähoito',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm02') from parent where parent.oid is not null)
select createOrganisaatio('1.2.246.562.99.00000000014','Metsämarja testi ryhmäperhepäiväkoti',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm03') from parent where parent.oid is not null
union all select * from a union all select * from b;


with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000015','Varhaiskasvatuksen järjestäjä yksityinen', '1.2.246.562.10.00000000001', 0, '|0|', array['organisaatiotyyppi_07'], null, null, 'Yksityinen elinkeinonharjoittaja', null) as foo(id bigint,oid varchar, parentidpath varchar)),
    a as (select createOrganisaatio('1.2.246.562.99.00000000016','Metsärousku testi päiväkoti',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm01') from parent where parent.oid is not null),
    b as (select createOrganisaatio('1.2.246.562.99.00000000017','Metsävahvero testi perhepäivähoito',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm02') from parent where parent.oid is not null)
select createOrganisaatio('1.2.246.562.99.00000000018','Metsähapero testi ryhmäperhepäiväkoti',parent.oid,parent.id,concat(parent.parentidpath,parent.id,'|'), array['organisaatiotyyppi_08'], null, null, null, 'vardatoimintamuoto_tm03') from parent where parent.oid is not null
union all select * from a union all select * from b;
