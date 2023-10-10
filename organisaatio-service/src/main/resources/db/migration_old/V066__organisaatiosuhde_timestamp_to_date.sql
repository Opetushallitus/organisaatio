--
-- Muutetaan organisaatiosuhde taulun alkupvm ja loppupvm oleman DATE TIMESTAMP sijaan
--

ALTER TABLE organisaatiosuhde ADD COLUMN alkupvmdate DATE;
ALTER TABLE organisaatiosuhde ADD COLUMN loppupvmdate DATE;

update organisaatiosuhde  set alkupvmdate = cast(alkupvm as date) where alkupvm is not null;
update organisaatiosuhde  set loppupvmdate = cast(loppupvm as date) where loppupvm is not null;

ALTER TABLE organisaatiosuhde DROP COLUMN alkupvm;
ALTER TABLE organisaatiosuhde DROP COLUMN loppupvm;

ALTER TABLE organisaatiosuhde RENAME COLUMN alkupvmdate TO alkupvm;
ALTER TABLE organisaatiosuhde RENAME COLUMN loppupvmdate TO loppupvm;

