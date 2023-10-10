    create table organisaatio_ryhmatyypit (
        organisaatio_id int8 not null,
        ryhmatyypit varchar(255)
    );

    create table organisaatio_kayttoryhmat (
        organisaatio_id int8 not null,
        kayttoryhmat varchar(255)
    );

    alter table organisaatio_ryhmatyypit 
        add constraint FK0F14B15F3F4B641B 
        foreign key (organisaatio_id) 
        references Organisaatio;

    alter table organisaatio_kayttoryhmat 
        add constraint FK5A76E4323F4B641B
        foreign key (organisaatio_id) 
        references Organisaatio;
