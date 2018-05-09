create table lisatietotyyppi (
	id int8 not null,
	version int8 not null,
	nimi varchar(255) unique,
	primary key (id)
);

create table organisaatio_lisatieto (
	id int8 not null,
	version int8 not null,
	arvo varchar(255),
	lisatietotyyppi_id int8 not null,
	organisaatio_id int8,
	primary key (id)
);

create table rajoite (
	rajoitetyyppi varchar(31) not null,
	id int8 not null,
	version int8 not null,
	arvo varchar(255),
	lisatietotyyppi_id int8,
	primary key (id)
);
