CREATE TABLE IF NOT EXISTS lisatietotyyppi (
  id int8 not null unique,
  version int8 not null,
  nimi varchar(255) not null,
  organisaatiotyyppi_rajoite varchar(255),
  oppilaitostyyppi_rajoite varchar(255)
);

CREATE TABLE IF NOT EXISTS organisaatio_lisatieto (
  id int8 not null unique,
  version int8 not null,
  organisaatio_id int8 not null,
  lisatietotyyppi_id int8 not null,
  arvo varchar(255)
);
