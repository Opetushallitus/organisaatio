--
-- Poistetaan duplikaattirivit taulusta organisaatio_tyypit
--

DELETE FROM organisaatio_tyypit a
USING (
   SELECT MIN(ctid) as ctid, organisaatio_id, tyypit
   FROM organisaatio_tyypit
   GROUP BY organisaatio_id, tyypit
   HAVING COUNT(*) > 1
) b
WHERE a.organisaatio_id = b.organisaatio_id
AND a.tyypit = b.tyypit
AND a.ctid <> b.ctid
