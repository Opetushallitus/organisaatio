CREATE TABLE queuedemailstatus (
    id text PRIMARY KEY,
    description text NOT NULL
);

INSERT INTO queuedemailstatus (id, description) VALUES
('QUEUED', 'Sähköposti odottaa lähettämistä'),
('SENT', 'Sähköposti on lähetetty viestinvälityspalveluun'),
('ERROR', 'Sähköposti on virheellinen');

CREATE TABLE queuedemail (
    id uuid PRIMARY KEY,
    queuedemailstatus_id text NOT NULL REFERENCES queuedemailstatus (id),
    recipients text[] NOT NULL CONSTRAINT recipients_required CHECK (recipients != '{}'),
    replyto text,
    subject text NOT NULL,
    body text NOT NULL,
    lahetystunniste text,
    copy text,
    created timestamp NOT NULL DEFAULT current_timestamp,
    modified timestamp NOT NULL DEFAULT current_timestamp,
    last_attempt timestamp,
    sent_at timestamp,
    batch_sent int NOT NULL default 0,
    idempotency_key uuid NOT NULL,
    CONSTRAINT sent_has_to_have_lahetystunniste CHECK ((queuedemailstatus_id = 'SENT' AND lahetystunniste IS NOT NULL) OR queuedemailstatus_id != 'SENT'),
    CONSTRAINT sent_at_requires_status_sent CHECK ((queuedemailstatus_id = 'SENT' AND sent_at IS NOT NULL) OR queuedemailstatus_id != 'SENT')
);
