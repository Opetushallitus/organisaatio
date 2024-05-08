ALTER TABLE queuedemail
    ADD COLUMN batch_sent int not null default 0,
    DROP CONSTRAINT queued_cant_have_lahetystunniste;