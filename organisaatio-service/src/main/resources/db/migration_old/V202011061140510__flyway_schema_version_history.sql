DO $$
    BEGIN
        IF EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'schema_version'
            )
        AND NOT EXISTS
            ( SELECT 1
              FROM   information_schema.tables
              WHERE  table_schema = 'public'
              AND    table_name = 'schema_version_history'
            )
        THEN
            CREATE TABLE schema_version_history AS
            TABLE schema_version;

            ALTER TABLE "schema_version_history" DROP COLUMN "version_rank";
            ALTER TABLE "schema_version_history" ALTER COLUMN "version" DROP NOT NULL;
            ALTER TABLE "schema_version_history" ADD CONSTRAINT "schema_version_history_pk" PRIMARY KEY ("installed_rank");
            UPDATE "schema_version_history" SET "type"='BASELINE' WHERE "type"='INIT';

            update "schema_version_history" set checksum=-1286338546 where version='000';
            update "schema_version_history" set checksum=-1512320181 where version='001';
            update "schema_version_history" set checksum=-211508128 where version='002';
            update "schema_version_history" set checksum=-756644467 where version='003';
            update "schema_version_history" set checksum=2102055068 where version='004';
            update "schema_version_history" set checksum=1944906814 where version='007';
            update "schema_version_history" set checksum=1152460306 where version='008';
            update "schema_version_history" set checksum=-1457769628 where version='010';
            update "schema_version_history" set checksum=826248184 where version='013';
            update "schema_version_history" set checksum=361689311 where version='015';
            update "schema_version_history" set checksum=21041079 where version='016';
            update "schema_version_history" set checksum=-752850120 where version='017';
            update "schema_version_history" set checksum=-32513174 where version='018';
            update "schema_version_history" set checksum=-619279000 where version='019';
            update "schema_version_history" set checksum=-98361985 where version='020';
            update "schema_version_history" set checksum=-1055022489 where version='022';
            update "schema_version_history" set checksum=-677649392 where version='024';
            update "schema_version_history" set checksum=78987744 where version='025';
            update "schema_version_history" set checksum=400273610 where version='026';
            update "schema_version_history" set checksum=-1365847302 where version='027';
            update "schema_version_history" set checksum=1488885358 where version='028';
            update "schema_version_history" set checksum=1670076462 where version='029';
            update "schema_version_history" set checksum=-797374776 where version='030';
            update "schema_version_history" set checksum=-1251764691 where version='031';
            update "schema_version_history" set checksum=-1201511912 where version='032';
            update "schema_version_history" set checksum=308695123 where version='033';
            update "schema_version_history" set checksum=-1510295189 where version='034';
            update "schema_version_history" set checksum=652871176 where version='035';
            update "schema_version_history" set checksum=186318554 where version='036';
            update "schema_version_history" set checksum=-1545125925 where version='037';
            update "schema_version_history" set checksum=-999670351 where version='038';
            update "schema_version_history" set checksum=1261231715 where version='039';
            update "schema_version_history" set checksum=-1560593038 where version='042';
            update "schema_version_history" set checksum=-782320183 where version='044';
            update "schema_version_history" set checksum=1250292427 where version='045';
            update "schema_version_history" set checksum=-781495142 where version='049';
            update "schema_version_history" set checksum=-795714661 where version='050';
            update "schema_version_history" set checksum=-1211289342 where version='051';
            update "schema_version_history" set checksum=1601474499 where version='052';
            update "schema_version_history" set checksum=-339438193 where version='053';
            update "schema_version_history" set checksum=327713423 where version='054';
            update "schema_version_history" set checksum=104282769 where version='056';
            update "schema_version_history" set checksum=1672075407 where version='057';
            update "schema_version_history" set checksum=1085475599 where version='058';
            update "schema_version_history" set checksum=682763497 where version='059';
            update "schema_version_history" set checksum=1748470905 where version='060';
            update "schema_version_history" set checksum=1927107645 where version='061';
            update "schema_version_history" set checksum=1346336087 where version='062';
            update "schema_version_history" set checksum=-353207125 where version='063';
            update "schema_version_history" set checksum=820233497 where version='064';
            update "schema_version_history" set checksum=-645565244 where version='065';
            update "schema_version_history" set checksum=-289872733 where version='066';
            update "schema_version_history" set checksum=50830459 where version='067';
            update "schema_version_history" set checksum=448158891 where version='068';
            update "schema_version_history" set checksum=1992344473 where version='069';
            update "schema_version_history" set checksum=-180043602 where version='070';
            update "schema_version_history" set checksum=-219676626 where version='071';
            update "schema_version_history" set checksum=8094616 where version='072';
            update "schema_version_history" set checksum=526704273 where version='073';
            update "schema_version_history" set checksum=355881163 where version='074';
            update "schema_version_history" set checksum=-1806784419 where version='076';
            update "schema_version_history" set checksum=-526442423 where version='077';
            update "schema_version_history" set checksum=283661412 where version='078';
            update "schema_version_history" set checksum=196451456 where version='079';
            update "schema_version_history" set checksum=1873624569 where version='080';
            update "schema_version_history" set checksum=-43404238 where version='081';
            update "schema_version_history" set checksum=-1468250861 where version='082';
            update "schema_version_history" set checksum=-350289277 where version='083';
            update "schema_version_history" set checksum=578966854 where version='084';
            update "schema_version_history" set checksum=1457273963 where version='085';
            update "schema_version_history" set checksum=777624186 where version='086';
            update "schema_version_history" set checksum=-600003505 where version='087';
            update "schema_version_history" set checksum=322311963 where version='088';
            update "schema_version_history" set checksum=-1187266367 where version='089';
            update "schema_version_history" set checksum=-1882425930 where version='2019051314300712';
            update "schema_version_history" set checksum=-1945414197 where version='20191004102619643';
            update "schema_version_history" set checksum=-913416188 where version='011';
            update "schema_version_history" set checksum=146134344 where version='040';
            update "schema_version_history" set checksum=-1529477566 where version='041';
            update "schema_version_history" set checksum=-1249858660 where version='20191111073438548';
            update "schema_version_history" set checksum=867062117 where version='20190919113950693';
            update "schema_version_history" set checksum=273753243 where version='20190919114313597';
            update "schema_version_history" set checksum=-415434808 where version='20191209144916432';
            update "schema_version_history" set checksum=-1829628806 where version='20191210095343849';
            update "schema_version_history" set checksum=2075116862 where version='20200401094951928';
            update "schema_version_history" set checksum=-2065323986 where version='20200403122403621';
        END IF ;
    END
   $$ ;