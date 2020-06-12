--
-- Organisaatioiden väliseen liitosten hallintaan
--

CREATE TABLE organisaatioliitos (
  id          INT8         NOT NULL UNIQUE,
  version     INT8         NOT NULL,
  alkuPvm     DATE         NOT NULL,
  organisaatio_id    INT8         NOT NULL,
  kohde_id    INT8         NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE organisaatioliitos
ADD CONSTRAINT FKF600EE483BA9203E
FOREIGN KEY (organisaatio_id)
REFERENCES Organisaatio;

ALTER TABLE organisaatioliitos
ADD CONSTRAINT FKF600EE485425B9F0
FOREIGN KEY (kohde_id)
REFERENCES Organisaatio;
