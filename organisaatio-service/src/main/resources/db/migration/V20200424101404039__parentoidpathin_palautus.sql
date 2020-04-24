ALTER TABLE organisaatio ADD COLUMN IF NOT EXISTS parentOidPath VARCHAR(512);

CREATE OR REPLACE FUNCTION setParentOidPaths() RETURNS VOID AS $$
DECLARE
  p RECORD;
BEGIN
  FOR p IN SELECT organisaatio_id, array_agg(parent_oid ORDER BY parent_position DESC) parent_oids FROM organisaatio_parent_oids GROUP BY organisaatio_id LOOP
    UPDATE organisaatio SET parentOidPath = '|' || array_to_string(p.parent_oids, '|') || '|' WHERE id = p.organisaatio_id;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT setParentOidPaths();
