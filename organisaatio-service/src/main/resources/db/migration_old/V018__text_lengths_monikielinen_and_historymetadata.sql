--
-- Increase the size of the translated text fields length (was 4096)
--
ALTER TABLE monikielinenteksti_values ALTER COLUMN "value" TYPE varchar(16384);

--
-- Just in case do the same for this too, but change it to LOB
--
ALTER TABLE history_metadata ALTER COLUMN arvo TYPE text;
