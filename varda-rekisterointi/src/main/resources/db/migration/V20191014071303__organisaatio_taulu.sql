CREATE TABLE organisaatio (
    rekisterointi_id BIGINT NOT NULL UNIQUE REFERENCES rekisterointi ON DELETE CASCADE,
    ytunnus text PRIMARY KEY,
    oid text UNIQUE,
    alkupvm DATE NOT NULL,
    toimintamuoto text NOT NULL,
    tyyppi text NOT NULL,
    kotipaikka text NOT NULL,
    maa text NOT NULL,
    nimi text NOT NULL,
    nimi_kieli CHAR(2) NOT NULL DEFAULT 'fi',
    nimi_alkupvm DATE NOT NULL
);

ALTER TABLE rekisterointi DROP COLUMN organisaatio;
ALTER TABLE rekisterointi DROP COLUMN toimintamuoto;
