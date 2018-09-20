WITH RECURSIVE aliorganisaatiot AS (
    SELECT id, oid
    FROM organisaatio o
    WHERE oid = :oid
UNION
    SELECT o.id, o.oid
    FROM organisaatio o
    JOIN organisaatiosuhde os ON os.child_id = o.id
    JOIN aliorganisaatiot ao ON ao.id = os.parent_id
    WHERE os.suhdetyyppi <> 'LIITOS'
    AND (os.loppupvm IS NULL OR os.loppupvm > :paivamaara)
    AND o.organisaatiopoistettu = FALSE
    AND (
        (:aktiiviset = TRUE AND (o.alkupvm IS NULL OR o.alkupvm <= :paivamaara)
                            AND (o.lakkautuspvm IS NULL OR o.lakkautuspvm > :paivamaara))
        OR
        (:suunnitellut = TRUE AND (o.alkupvm > :paivamaara))
        OR
        (:lakkautetut = TRUE AND (o.lakkautuspvm <= :paivamaara))
    )
)
SELECT oid FROM aliorganisaatiot;
