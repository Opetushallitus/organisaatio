CREATE TABLE rekisterointi (
    id bigserial PRIMARY KEY,
    organisaatio jsonb NOT NULL,
    toimintamuoto text NOT NULL,
    sahkopostit text[] NOT NULL
);

CREATE TABLE kayttaja (
    id bigserial PRIMARY KEY,
    etunimi text NOT NULL,
    sukunimi text NOT NULL,
    sahkoposti text NOT NULL,
    asiointikieli char(2) NOT NULL,
    saateteksti text,
    rekisterointi bigint UNIQUE REFERENCES rekisterointi NOT NULL
);
