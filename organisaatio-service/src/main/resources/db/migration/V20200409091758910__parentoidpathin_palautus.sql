ALTER TABLE organisaatio ADD COLUMN IF NOT EXISTS parentOidPath VARCHAR(512);

CREATE OR REPLACE FUNCTION updateParentOidPaths() RETURNS VOID AS $$
DECLARE
  _organisaatioId BIGINT;
  _parentOidPath VARCHAR(255);
BEGIN
  FOR _organisaatioId, _parentOidPath IN SELECT organisaatio_id, '|' || string_agg(parent_oid, '|' ORDER BY parent_position DESC) || '|' FROM organisaatio_parent_oids GROUP BY organisaatio_id LOOP
    UPDATE organisaatio SET parentOidPath = _parentOidPath WHERE id = _organisaatioId;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT updateParentOidPaths();

UPDATE organisaatio SET parentOidPath = '' WHERE parentOidPath IS NULL;
ALTER TABLE organisaatio ALTER COLUMN parentOidPath SET NOT NULL;
CREATE INDEX ON organisaatio (parentOidPath);

DROP FUNCTION IF EXISTS updateParentOidPaths();
DROP FUNCTION IF EXISTS insertParentOids();
DROP TABLE IF EXISTS organisaatio_parent_oids;
