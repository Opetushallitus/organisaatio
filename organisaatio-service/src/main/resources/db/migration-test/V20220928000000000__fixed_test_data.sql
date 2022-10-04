CREATE OR REPLACE FUNCTION createOrganisaatio(oid1 varchar, nimi varchar, parentoid varchar, parentid bigint, parentidpath varchar, types varchar[], oppilaitostyyppi varchar) returns RECORD
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
        org     AS (INSERT into organisaatio (oid,id,version, nimi_mkt, piilotettu, organisaatiopoistettu, paivityspvm, paivittaja, alkupvm, kotipaikka, maa, parentidpath,oppilaitostyyppi, nimihaku)
                    SELECT  data.oid, (select max(id)+1 from organisaatio),0, mkt.id, false, false, CURRENT_TIMESTAMP, data.paivittaja, CURRENT_DATE, 'kunta_153', 'maatjavaltiot1_fin', parentidpath, oppilaitostyyppi, createOrganisaatio.nimi FROM mkt join data on 1=1 RETURNING id, oid, organisaatio.parentidpath ),
        orgnim  AS (INSERT INTO organisaatio_nimi (id, version, alkupvm, organisaatio_id, nimi_mkt, paivittaja) SELECT (select max(id)+1 from organisaatio_nimi),0, CURRENT_DATE, org.id, mkt.id, data.paivittaja  from org join mkt on 1=1 join data on 1=1 returning id),
        orgtyp  AS (INSERT INTO organisaatio_tyypit (organisaatio_id, tyypit) SELECT org.id, typ.id from org join unnest(types) as typ(id) on 1=1 returning organisaatio_tyypit.organisaatio_id, organisaatio_tyypit.tyypit),
        kieli1  AS (INSERT INTO organisaatio_kielet (organisaatio_id, kielet) SELECT id, 'oppilaitoksenopetuskieli_1' from org),
        parent  AS (INSERT INTO organisaatio_parent_oids (organisaatio_id, parent_position, parent_oid) SELECT org.id, 0, parentoid from org),
        suhde   AS (INSERT INTO organisaatiosuhde (id, version, suhdetyyppi, child_id, parent_id, alkupvm) SELECT (select max(id)+1 from organisaatiosuhde),0,'HISTORIA',org.id,parentid,CURRENT_DATE from org returning organisaatiosuhde.id),
        yhteys  AS (INSERT INTO yhteystieto (dtype, id, version,yhteystietooid, osoite, osoitetyyppi, postinumero, postitoimipaikka,email, organisaatio_id, kieli)
                    SELECT osoite.type, (select max(id)+1 from yhteystieto)+osoite.number,0, osoite.oid,osoite.osoite, osoite.osoitetyyppi,osoite.postinumero, osoite.postitoimipaikka, osoite.email,org.id,osoite.kieli
                    FROM org,
                         (values ('Osoite',concat(oid1,'.1'),'Testikuja 5','posti','posti_00009','DIGITOINTI',null,'kieli_fi#1',1),
                                 ('Email' ,concat(oid1,'.2'),null,null,null,null,'testiposti@opetushallitus.fi','kieli_fi#1',2),
                                 ('Osoite',concat(oid1,'.3'),'Testikuja 5','kaynti','posti_00009','DIGITOINTI',null,'kieli_fi#1',3))
                             AS osoite(type, oid, osoite, osoitetyyppi, postinumero, postitoimipaikka, email, kieli, number))
    SELECT org.* into ret FROM org;
    return ret;
end;
$$;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000001','Mansikkalan testi kunta','1.2.246.562.10.00000000001',0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_09'], null) as foo(id bigint,oid varchar, parentidpath varchar)),
     a as (select createOrganisaatio('1.2.246.562.99.00000000002','Mansikkalan testi peruskoulu',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], null) from parent where parent.oid is not null ),
     b as (select createOrganisaatio('1.2.246.562.99.00000000003','Mansikkalan testi lukio',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], null) from parent where parent.oid is not null)
select createOrganisaatio('1.2.246.562.99.00000000004','Ahomansikan testi peruskoulu',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], null) from parent where parent.oid is not null union all select * from a union all select * from b;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000005','Testi Koulutuskuntayhtymä Puolukka','1.2.246.562.10.00000000001',0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_09'], null) as foo(id bigint,oid varchar, parentidpath varchar)),
     a as (select createOrganisaatio('1.2.246.562.99.00000000006','Testi Ammattiopisto Puolukka',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_21#1') from parent where parent.oid is not null )
select createOrganisaatio('1.2.246.562.99.00000000007','Testi Puolukkalan liikuntaopisto',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_62#1') from parent where parent.oid is not null union all select * from a ;

with parent as (select id,oid,parentidpath from createOrganisaatio('1.2.246.562.99.00000000008','Mustikkalan testi yhdistys','1.2.246.562.10.00000000001',0, '|0|', array['organisaatiotyyppi_01','organisaatiotyyppi_05'], null) as foo(id bigint,oid varchar, parentidpath varchar))
select createOrganisaatio('1.2.246.562.99.00000000009','Mustikkalan testi opisto',parent.oid,parent.id,concat(parent.parentidpath,'|',parent.id), array['organisaatiotyyppi_02'], 'oppilaitostyyppi_64#1') from parent where parent.oid is not null ;
