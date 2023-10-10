alter table organisaatio_kielet rename to organisaatio_kielet_old;
create table organisaatio_kielet (
    organisaatio_id bigint not null references organisaatio,
    kielet varchar(255) not null,
    primary key (organisaatio_id, kielet)
);
insert into organisaatio_kielet select distinct * from organisaatio_kielet_old where kielet is not null;
drop table organisaatio_kielet_old;
