CREATE TABLE queuedemailstatus (
    id text PRIMARY KEY,
    description text NOT NULL
);

INSERT INTO queuedemailstatus (id, description) VALUES
('QUEUED', 'Sähköposti odottaa lähettämistä'),
('SENT', 'Sähköposti on lähetetty viestinvälityspalveluun');

CREATE TABLE queuedemail (
    id text PRIMARY KEY,
    queuedemailstatus_id text NOT NULL REFERENCES queuedemailstatus (id),
    recipients text[] NOT NULL CONSTRAINT recipients_required CHECK (recipients != '{}'),
    replyto text NOT NULL,
    subject text NOT NULL,
    body text NOT NULL,
    lahetystunniste text,
    created timestamp NOT NULL DEFAULT current_timestamp,
    modified timestamp NOT NULL DEFAULT current_timestamp,
    CONSTRAINT sent_has_to_have_lahetystunniste CHECK ((queuedemailstatus_id = 'SENT' AND lahetystunniste IS NOT NULL) OR queuedemailstatus_id != 'SENT'),
    CONSTRAINT queued_cant_have_lahetystunniste CHECK ((queuedemailstatus_id = 'QUEUED' AND lahetystunniste IS NULL) OR queuedemailstatus_id != 'QUEUED')
);
