insert into monikielinenteksti (id, version) values (0, 1);
insert into monikielinenteksti_values (id, value, key) values (0, 'Opetushallitus', 'fi');
insert into monikielinenteksti_values (id, value, key) values (0, 'Utbildningsstyrelsen', 'sv');
insert into monikielinenteksti_values (id, value, key) values (0, 'The Finnish National Board of Education', 'en');
insert into organisaatio (id, version, alkuPvm, nimihaku, oid, organisaatioPoistettu, nimi_mkt)
    values (0, 0, '1970-01-01', 'oph,,', '1.2.246.562.24.00000000001', FALSE, 0);
insert into organisaatio_tyypit (organisaatio_id, tyypit) values (0, 'Muu organisaatio');
