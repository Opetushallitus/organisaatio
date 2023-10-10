--
-- OH-659: Kaipaisten koulu ei ole yhdistynyt Utin kouluun
--
DELETE FROM
    organisaatiosuhde
WHERE
    suhdetyyppi = 'LIITOS'
    AND parent_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.37251999811')
    AND child_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.77090067297');
