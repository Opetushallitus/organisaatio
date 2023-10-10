INSERT INTO monikielinenteksti_values (id, value, key, index)
WITH nimi AS (
    SELECT DISTINCT ON (mkt.id) mkt.id AS monikielinenteksti_id, mktv.value
    FROM organisaatio o
    JOIN monikielinenteksti mkt ON mkt.id = o.nimi_mkt
    JOIN monikielinenteksti_values mktv ON mktv.id = mkt.id
    WHERE o.id IN (SELECT organisaatio_id FROM organisaatio_tyypit WHERE tyypit = 'organisaatiotyyppi_02')
    ORDER BY mkt.id, CASE mktv.key
        WHEN 'fi' THEN 1
        WHEN 'sv' THEN 2
        WHEN 'en' THEN 3
        ELSE 9
        END)
SELECT monikielinenteksti_id, value, unnest(ARRAY['fi', 'sv', 'en']), 0
FROM nimi
ON CONFLICT ON CONSTRAINT monikielinenteksti_values_pkey DO NOTHING;
