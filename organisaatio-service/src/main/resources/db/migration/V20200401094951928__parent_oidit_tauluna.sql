CREATE TABLE organisaatio_parent_oids (
  organisaatio_id BIGINT NOT NULL REFERENCES organisaatio (id) ON DELETE CASCADE,
  parent_position INTEGER NOT NULL,
  parent_oid VARCHAR(255) NOT NULL,
  UNIQUE (organisaatio_id, parent_position),
  UNIQUE (organisaatio_id, parent_oid)
);

CREATE INDEX organisaatio_parent_oids_fk_idx ON organisaatio_parent_oids (organisaatio_id);
CREATE INDEX organisaatio_parent_oids_parent_idx ON organisaatio_parent_oids (parent_oid);

CREATE OR REPLACE FUNCTION insertParentOids() RETURNS VOID AS $$
DECLARE
  o organisaatio%ROWTYPE;
  _parentOids TEXT[];
  _parentCount SMALLINT;
  _position SMALLINT;
  _parentOid TEXT;
BEGIN
  FOR o IN SELECT * FROM organisaatio WHERE parentoidpath IS NOT NULL AND parentoidpath <> '' LOOP
    SELECT regexp_split_to_array(substring(o.parentoidpath FROM 2 FOR length(o.parentoidpath) -2), '\|') INTO STRICT _parentOids;
    SELECT array_length(_parentOids, 1) INTO STRICT _parentCount;
    _position = _parentCount - 1;
    FOREACH _parentOid IN ARRAY _parentOids LOOP
      INSERT INTO organisaatio_parent_oids (organisaatio_id, parent_position, parent_oid) VALUES (
        o.id, _position, _parentOid::VARCHAR(255)
      );
      _position = _position - 1;
    END LOOP;
  END LOOP;
END;
$$ LANGUAGE plpgsql;

SELECT insertParentOids();
