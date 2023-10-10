create table organisaatio_sahkoposti (
	id int8 not null,
	version int8 not null,
	aikaleima timestamp not null,
	tyyppi varchar(255) not null,
	viestintapalvelu_id varchar(255),
	organisaatio_id int8 not null,
	primary key (id)
);

alter table organisaatio_sahkoposti
	add constraint organisaatio_sahkoposti_organisaatio_id_fkey foreign key (organisaatio_id)references Organisaatio;
