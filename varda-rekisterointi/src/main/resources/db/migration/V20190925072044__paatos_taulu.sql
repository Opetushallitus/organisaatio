CREATE TABLE paatos (
    rekisterointi_id BIGINT NOT NULL UNIQUE REFERENCES rekisterointi ON DELETE CASCADE,
    hyvaksytty BOOLEAN NOT NULL,
    paatetty TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paattaja_id BIGINT NOT NULL UNIQUE REFERENCES kayttaja ON DELETE CASCADE,
    perustelu TEXT,
    PRIMARY KEY (rekisterointi_id)
);
