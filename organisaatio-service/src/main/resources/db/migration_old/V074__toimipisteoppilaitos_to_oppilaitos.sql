--
-- Poistetaan oppilaitokselta mahdollinen "toimipiste"-organisaatiotyyppi.
--

-- Poista "Toimipiste" tieto oppilaitokselta organisaatio taulusta
UPDATE organisaatio
SET organisaatiotyypitstr = 'Oppilaitos|'
WHERE organisaatiotyypitstr LIKE '%Oppilaitos%Toimipiste%'
OR organisaatiotyypitstr LIKE '%Toimipiste%Oppilaitos%';

-- Poista "Toimipiste" tieto oppilaitokselta organisaatio_tyypit taulusta
DELETE FROM organisaatio_tyypit
WHERE organisaatio_id IN
(
    SELECT tyypit_opl.organisaatio_id
    FROM organisaatio_tyypit tyypit_opl
    LEFT JOIN organisaatio_tyypit tyypit_tp
    ON tyypit_opl.organisaatio_id = tyypit_tp.organisaatio_id
    WHERE tyypit_tp.tyypit = 'Toimipiste'
    AND tyypit_opl.tyypit = 'Oppilaitos'
)
AND tyypit = 'Toimipiste';
