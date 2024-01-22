ALTER TABLE queuedemail
    ADD COLUMN last_attempt timestamp,
    ADD COLUMN sent_at timestamp;

UPDATE queuedemail SET last_attempt = modified;
UPDATE queuedemail SET sent_at = modified WHERE queuedemailstatus_id = 'SENT';

ALTER TABLE queuedemail
    ADD CONSTRAINT sent_at_requires_status_sent CHECK ((queuedemailstatus_id = 'SENT' AND sent_at IS NOT NULL) OR queuedemailstatus_id != 'SENT');
