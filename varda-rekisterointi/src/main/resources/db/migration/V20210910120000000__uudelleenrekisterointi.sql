ALTER TABLE organisaatio ADD COLUMN uudelleen_rekisterointi BOOLEAN NOT NULL DEFAULT false;
COMMENT ON COLUMN organisaatio.uudelleen_rekisterointi IS 'Onko kyseessä uudelleenrekisteröinti';

ALTER TABLE organisaatio DROP CONSTRAINT organisaatio_oid_key;
CREATE UNIQUE INDEX organisaatio_oid_key ON organisaatio (oid) WHERE uudelleen_rekisterointi = FALSE;
