CREATE TABLE yhteystiedot (
    rekisterointi_id BIGINT NOT NULL UNIQUE REFERENCES organisaatio ON DELETE CASCADE,
    puhelinnumero TEXT NOT NULL,
    sahkoposti TEXT NOT NULL,
    posti_katuosoite TEXT NOT NULL,
    posti_postinumero_uri TEXT NOT NULL,
    posti_postitoimipaikka TEXT NOT NULL,
    kaynti_katuosoite TEXT NOT NULL,
    kaynti_postinumero_uri TEXT NOT NULL,
    kaynti_postitoimipaikka TEXT NOT NULL,
    PRIMARY KEY (rekisterointi_id)
)
