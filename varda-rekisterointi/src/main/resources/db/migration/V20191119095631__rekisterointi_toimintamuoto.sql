ALTER TABLE rekisterointi ADD COLUMN toimintamuoto text;
UPDATE rekisterointi SET toimintamuoto = 'vardatoimintamuoto_tm01';
ALTER TABLE rekisterointi ALTER COLUMN toimintamuoto SET NOT NULL;
