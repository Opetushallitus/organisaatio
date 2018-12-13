create table varhaiskasvatuksen_toimipaikka_tiedot (
  id bigint not null,
  version bigint not null,
  jarjestamismuoto varchar (255) not null,
  kasvatusopillinen_jarjestelma varchar (255) not null,
  toiminnallinen_painotus varchar (255) not null,
  paikkojen_lukumaara bigint not null,
  primary key (id)
);

create table varhaiskasvatuksen_kielipainotus (
  id bigint not null,
  version bigint not null,
  varhaiskasvatuksen_toimipaikka_tiedot_id bigint not null references varhaiskasvatuksen_toimipaikka_tiedot,
  kielipainotus varchar (255) not null,
  alkupvm timestamp not null,
  loppupvm timestamp not null,
  primary key (id)
);

create table varhaiskasvatuksen_toiminnallinenpainotus (
  id bigint not null,
  version bigint not null,
  varhaiskasvatuksen_toimipaikka_tiedot_id bigint not null references varhaiskasvatuksen_toimipaikka_tiedot,
  toiminnallinenpainotus varchar (255) not null,
  alkupvm timestamp not null,
  loppupvm timestamp not null,
  primary key (id)
);

create table varhaiskasvatuksen_toimintamuoto (
  varhaiskasvatuksen_toimipaikka_tiedot_id bigint not null references varhaiskasvatuksen_toimipaikka_tiedot,
  toimintamuoto varchar (255) not null
);

alter table organisaatio
  add column varhaiskasvatuksen_toimipaikka_tiedot_id bigint references varhaiskasvatuksen_toimipaikka_tiedot;
