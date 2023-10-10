--
-- Historiadataa ajallisesti, esim. organisaation kieli
--

CREATE TABLE history_metadata (
  id              INT8         NOT NULL UNIQUE,
  version         INT8         NOT NULL,
  aika            TIMESTAMP    NOT NULL,
  arvo            VARCHAR(2048),
  avain           VARCHAR(255) NOT NULL,
  kieli           VARCHAR(255) NOT NULL,
  organisaatio_id INT8         NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (organisaatio_id, avain, kieli, aika)
);

CREATE INDEX history_metadata_aika_idx ON history_metadata (aika);
CREATE INDEX history_metadata_avain_idx ON history_metadata (avain);
CREATE INDEX history_metadata_kieli_idx ON history_metadata (kieli);

ALTER TABLE history_metadata
ADD CONSTRAINT FKD68AE7A3F4B641B
FOREIGN KEY (organisaatio_id)
REFERENCES Organisaatio;
