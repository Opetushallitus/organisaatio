create table if not exists lisatietotyyppi (
	id int8 not null,
	version int8 not null,
	nimi varchar(255) unique,
	primary key (id)
);

create table if not exists organisaatio_lisatieto (
	id int8 not null,
	version int8 not null,
  organisaatio_id int8 references organisaatio (id),
  lisatietotyyppi_id int8 references lisatietotyyppi (id),
  primary key (id)
);

create table if not exists rajoite (
	rajoitetyyppi varchar(31) not null,
	id int8 not null,
	version int8 not null,
	arvo varchar(255),
	lisatietotyyppi_id int8 references lisatietotyyppi (id),
	primary key (id)
);
