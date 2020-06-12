
create table organisaatio_muut_kotipaikat_uris (
	organisaatio_id int8 not null,
	muut_kotipaikat varchar(255) not null,
	primary key (organisaatio_id,muut_kotipaikat)
);

alter table organisaatio_muut_kotipaikat_uris
	add constraint FK4nfmp4pb08x2vntptw88pgcu7 foreign key (organisaatio_id)references Organisaatio;