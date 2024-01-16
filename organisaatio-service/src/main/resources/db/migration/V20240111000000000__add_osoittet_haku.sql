CREATE table osoitteet_haku_and_hakutulos (
    id uuid PRIMARY KEY,
    organisaatiotyypit text[] NOT NULL,
    oppilaitostyypit text[] NOT NULL,
    vuosiluokat text[] NOT NULL,
    kunnat text[] NOT NULL,
    anyJarjestamislupa boolean NOT NULL,
    jarjestamisluvat text[] NOT NULL,
    kielet text[] NOT NULL,
    organisaatio_ids bigint[] NOT NULL
);