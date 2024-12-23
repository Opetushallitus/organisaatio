ALTER TABLE rekisterointi ADD COLUMN kunnat text[];
UPDATE rekisterointi SET kunnat = '{}';
ALTER TABLE rekisterointi ALTER COLUMN kunnat SET NOT NULL;
