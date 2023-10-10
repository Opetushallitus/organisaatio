-- OH-660/1a Pohjoisten koulu ei ole yhdistynyt Syrjäntaan kouluun
DELETE FROM
    organisaatiosuhde
WHERE
    suhdetyyppi = 'LIITOS'
    AND parent_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.76152598975')
    AND child_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.74785893533');

-- OH-660/1b Syrjäntaan koulua ei ole uudelleennimetty
DELETE FROM
    organisaatio_nimi
WHERE
    organisaatio_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.76152598975')
    AND alkupvm > '1992-01-01';

UPDATE organisaatio
SET nimi_mkt = (
    SELECT nimi_mkt FROM organisaatio_nimi
    WHERE organisaatio_id = (SELECT id FROM organisaatio WHERE oid = '1.2.246.562.10.76152598975'))
WHERE oid = '1.2.246.562.10.76152598975';

-- OH-660/2a Pohjoiste koulua EI ole passivoitu
UPDATE organisaatio
SET lakkautuspvm = NULL
WHERE oid = '1.2.246.562.10.74785893533';

