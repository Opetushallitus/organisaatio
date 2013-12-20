ALTER TABLE organisaatiosuhde ADD COLUMN paivityspvm DATE;
ALTER TABLE organisaatiosuhde ALTER COLUMN paivityspvm SET DEFAULT current_date;

ALTER TABLE organisaatiosuhde ADD COLUMN paivittaja VARCHAR(255);
ALTER TABLE organisaatiosuhde ALTER COLUMN paivittaja SET DEFAULT current_user;

UPDATE organisaatiosuhde SET paivityspvm = current_date, paivittaja = 'oph';

-- rollback
-- ALTER TABLE organisaatiosuhde DROP COLUMN paivityspvm;
-- ALTER TABLE organisaatiosuhde DROP COLUMN paivittaja;