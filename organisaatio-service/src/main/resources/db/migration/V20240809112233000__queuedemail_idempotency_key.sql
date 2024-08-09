ALTER TABLE queuedemail
ADD COLUMN idempotency_key uuid;

UPDATE queuedemail
SET idempotency_key = uuid_in(overlay(overlay(md5(random()::text || ':' || random()::text) placing '4' from 13) placing to_hex(floor(random()*(11-8+1) + 8)::int)::text from 17)::cstring);

ALTER TABLE queuedemail
ALTER COLUMN idempotency_key set not null;