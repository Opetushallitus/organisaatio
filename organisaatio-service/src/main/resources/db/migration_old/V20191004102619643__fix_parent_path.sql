WITH RECURSIVE aliorganisaatiot(id, oid, depth, parentoidpath, oid_path, parentidpath, id_path) AS (
    SELECT id, oid, 0, parentoidpath, ARRAY[oid::TEXT], parentidpath, ARRAY[id]
    FROM organisaatio o
    WHERE oid = '1.2.246.562.10.00000000001'
UNION
    SELECT o.id, o.oid, ao.depth + 1, o.parentoidpath, ARRAY_APPEND(ao.oid_path, o.oid::TEXT), o.parentidpath, ARRAY_APPEND(ao.id_path, o.id)
    FROM organisaatio o
    JOIN organisaatiosuhde os ON os.child_id = o.id
    JOIN aliorganisaatiot ao ON ao.id = os.parent_id
    WHERE os.suhdetyyppi <> 'LIITOS'
      AND os.alkupvm < current_date
      AND (os.loppupvm is null OR os.loppupvm > current_date)
)
UPDATE organisaatio o
SET parentoidpath = '|' || ARRAY_TO_STRING(ARRAY_REMOVE(ao.oid_path, o.oid::TEXT), '|') || '|',
    parentidpath = '|' || ARRAY_TO_STRING(ARRAY_REMOVE(ao.id_path, o.id), '|') || '|'
FROM aliorganisaatiot ao
WHERE ao.id = o.id
AND o.oid <> '1.2.246.562.10.00000000001'
AND (
    '|' || ARRAY_TO_STRING(ARRAY_REMOVE(ao.oid_path, o.oid::TEXT), '|') || '|' <> o.parentoidpath
    OR
    '|' || ARRAY_TO_STRING(ARRAY_REMOVE(ao.id_path, o.id), '|') || '|' <> o.parentidpath
);
