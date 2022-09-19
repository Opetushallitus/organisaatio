ALTER TABLE rekisterointi ADD COLUMN tyyppi text NOT NULL DEFAULT 'varda';
ALTER TABLE rekisterointi ALTER COLUMN tyyppi DROP DEFAULT;

ALTER TABLE rekisterointi ADD CONSTRAINT varda_constraint
  CHECK (CASE tyyppi WHEN 'varda' THEN toimintamuoto IS NOT NULL AND kunnat IS NOT NULL
                     ELSE true END);

ALTER TABLE rekisterointi ALTER COLUMN toimintamuoto DROP NOT NULL;
ALTER TABLE rekisterointi ALTER COLUMN kunnat DROP NOT NULL;
