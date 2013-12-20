--
-- Organisaatioiden väliseen historiallisten suhteiden hallintaan
--

CREATE TABLE organisaatiosuhde (
  id          INT8         NOT NULL UNIQUE,
  version     INT8         NOT NULL,
  alkuPvm     TIMESTAMP,
  loppuPvm    TIMESTAMP,
  suhdeTyyppi VARCHAR(255) NOT NULL,
  child_id    INT8         NOT NULL,
  parent_id   INT8         NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE organisaatiosuhde
ADD CONSTRAINT FKF600EE483BA9203E
FOREIGN KEY (child_id)
REFERENCES Organisaatio;

ALTER TABLE organisaatiosuhde
ADD CONSTRAINT FKF600EE485425B9F0
FOREIGN KEY (parent_id)
REFERENCES Organisaatio;
