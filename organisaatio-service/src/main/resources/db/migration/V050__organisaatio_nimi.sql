--
-- Organisaation nimi ja nimihistoria
--

CREATE TABLE organisaatio_nimi (
  id              INT8         NOT NULL UNIQUE,
  version         INT8         NOT NULL,
  alkuPvm         DATE         NOT NULL,
  organisaatio_id INT8         NOT NULL,
  nimi_mkt        INT8,
  paivittaja      VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE (organisaatio_id, alkuPvm, nimi_mkt)
);

CREATE INDEX organisaatio_nimi_organisaatio_id_idx ON organisaatio_nimi (organisaatio_id);
CREATE INDEX organisaatio_nimi_alkuPvm_idx ON organisaatio_nimi (alkuPvm);
CREATE INDEX organisaatio_nimi_organisaatio_id_alkuPvm_idx ON organisaatio_nimi (organisaatio_id, alkuPvm);

ALTER TABLE organisaatio_nimi
ADD CONSTRAINT FKD68AE7A3F4B641B
FOREIGN KEY (organisaatio_id)
REFERENCES Organisaatio;

ALTER TABLE organisaatio_nimi
ADD CONSTRAINT FK4415DA7F181D43A9
FOREIGN KEY (nimi_mkt)
REFERENCES MonikielinenTeksti;
