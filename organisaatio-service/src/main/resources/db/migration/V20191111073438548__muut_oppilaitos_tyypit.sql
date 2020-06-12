create table organisaatio_muut_oppilaitostyypit (
	organisaatio_id int8 not null,
	oppilaitostyyppi varchar(255) not null,
	primary key (organisaatio_id,oppilaitostyyppi)
);

alter table organisaatio_muut_oppilaitostyypit
	add constraint organisaatio_muut_oppilaitostyypit_organisaatio_id_fkey foreign key (organisaatio_id)references Organisaatio;
