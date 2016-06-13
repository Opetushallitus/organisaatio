CREATE TABLE ytjpaivitysloki (
  id              INT8         NOT NULL UNIQUE,
  version         INT8         NOT NULL,
  paivitysaika    TIMESTAMP    NOT NULL,
  paivitetyt_lkm  INT8,
  paivitys_tila   VARCHAR(255) NOT NULL,
  paivitys_tila_selite VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE ytjvirhe (
  id              INT8         NOT NULL UNIQUE,
  version         INT8         NOT NULL,
  oid             VARCHAR(255) NOT NULL,
  orgNimi         VARCHAR(255),
  ytjpaivitysloki_id INT8      NOT NULL,
  virhekohde     VARCHAR(255),
  virheviesti   VARCHAR(255),
  PRIMARY KEY (id)
);

ALTER TABLE ytjvirhe
ADD CONSTRAINT ytjvirhe_ytjpaivitys
FOREIGN KEY (ytjpaivitysloki_id)
REFERENCES ytjpaivitysloki;

CREATE INDEX ytjvirhe_oid_idx ON ytjvirhe (oid);
CREATE INDEX ytjpaivitysloki_paivitysaika_idx ON ytjpaivitysloki (paivitysaika);



