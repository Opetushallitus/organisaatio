update organisaatio_ryhmatyypit set ryhmatyypit = 'ryhmatyypit_1#1' where ryhmatyypit = 'organisaatio';
update organisaatio_ryhmatyypit set ryhmatyypit = 'ryhmatyypit_2#1' where ryhmatyypit = 'hakukohde';
update organisaatio_ryhmatyypit set ryhmatyypit = 'ryhmatyypit_3#1' where ryhmatyypit = 'perustetyoryhma';
update organisaatio_ryhmatyypit set ryhmatyypit = 'ryhmatyypit_4#1' where ryhmatyypit = 'koulutus';

update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_1#1' where kayttoryhmat = 'yleinen';
update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_2#1' where kayttoryhmat = 'hakukohde_rajaava';
update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_3#1' where kayttoryhmat = 'hakukohde_priorisoiva';
update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_4#1' where kayttoryhmat = 'hakukohde_liiteosoite';
update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_5#1' where kayttoryhmat = 'perusteiden_laadinta';
update organisaatio_kayttoryhmat set kayttoryhmat = 'kayttoryhmat_6#1' where kayttoryhmat = 'kayttooikeus';
