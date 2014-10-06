-- OPHASPA-1374: 
--     Oppilaitos Parolan lukio (00665) toimipisteineen on ollut Hattulan kunnan ylläpitämä 31.7.2014 asti. 
--     1.8.2014 Parolan lukio siirtyi Koulutus kuntayhtymä Tavastian ylläpitämäksi.

-- Parolan lukio (oppilaitos) on siirtynyt pois Hattulan kunnan alta
update organisaatiosuhde set loppupvm=timestamp '2014-07-31 23:59:59.999',paivityspvm=current_date where child_id=12570 and parent_id=790;

-- Parolan lukio (oppilaitos) on siirtynyt koulutuskuntayhtymä Tavastian alle
delete from organisaatiosuhde where child_id=12570 and parent_id=3226 and alkupvm=timestamp '2014-08-01 00:00:00.000';
insert into organisaatiosuhde (id, version, alkupvm, suhdetyyppi, child_id, parent_id, paivityspvm, paivittaja) values (nextval('public.hibernate_sequence'), 0, timestamp '2014-08-01 00:00:00.000', 'HISTORIA', 12570, 3226, current_date, 'oph' );

-- Päivitetään parolan lukion (oppilaitos) parent-tiedot
update organisaatio set parentidpath='|0|3226|', parentoidpath='|1.2.246.562.10.00000000001|1.2.246.562.10.83097956748|' where id=12570 and parentidpath='|0|790|';

-- Päivitetään parolan lukion (toimipiste) parent-tiedot
update organisaatio set parentidpath='|0|3226|12570', parentoidpath='|1.2.246.562.10.00000000001|1.2.246.562.10.83097956748|1.2.246.562.10.816380467310|' where id=41102 and parentidpath='|0|790|12570|'; 

