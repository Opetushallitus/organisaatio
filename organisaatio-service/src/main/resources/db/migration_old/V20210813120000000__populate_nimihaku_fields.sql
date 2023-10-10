--
-- OH-696: Ajastettu organisaation nimimuutos ei ole populoinut nimihaku
-- kenttää -> nimet ja nimihaku on epäsynkassa. Synkataan seuraavalla SQL:llä
--
UPDATE organisaatio o SET nimihaku = (
    SELECT STRING_AGG(value, ',') FROM monikielinenteksti_values mktv WHERE mktv.id = (
        SELECT org_nimi.nimi_mkt
        FROM organisaatio_nimi org_nimi
        WHERE org_nimi.alkupvm = (
            SELECT MAX(org_nimi2.alkupvm)
            FROM organisaatio_nimi org_nimi2
            WHERE org_nimi.organisaatio_id = org_nimi2.organisaatio_id
            AND org_nimi2.alkupvm <= current_date)
        AND org_nimi.organisaatio_id = o.id));
