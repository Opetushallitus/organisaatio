CREATE TABLE koodisto_koulutus (
    koodiuri text PRIMARY KEY,
    koodiarvo text NOT NULL,
    versio bigint NOT NULL,
    nimi_fi text,
    nimi_sv text,
    UNIQUE (koodiarvo)
);