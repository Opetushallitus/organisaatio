truncate table monikielinenteksti cascade;
truncate table monikielinenteksti_values cascade;
truncate table namedmonikielinenteksti cascade;
truncate table organisaatio cascade;
truncate table organisaatio_kayttoryhmat cascade;
truncate table organisaatio_kielet cascade;
truncate table organisaatio_nimi  cascade;
truncate table organisaatio_ryhmatyypit cascade;
truncate table organisaatio_tyypit cascade;
truncate table organisaatio_vuosiluokat cascade;
truncate table organisaatiometadata cascade;
truncate table organisaatiometadata_namedmonikielinenteksti cascade;
truncate table organisaatiometadata_yhteystieto cascade;
truncate table organisaatiosuhde cascade;
truncate table organisaatio_parent_oids cascade;
truncate table yhteystieto cascade;
truncate table yhteystietoarvo cascade;
truncate table yhteystietoelementti cascade;
truncate table yhteystietojentyyppi cascade;
truncate table yhteystietojentyyppi_oppilaitostyypit cascade;
truncate table yhteystietojentyyppi_organisaatiotyypit cascade;
truncate table ytjpaivitysloki cascade;
truncate table ytjvirhe cascade;
truncate table maakuntakuntarelation, koodisto_maakunta, koodisto_kunta;
truncate table koodisto_oppilaitostyyppi;
truncate table koodisto_oppilaitoksenopetuskieli;
truncate table koodisto_posti;
truncate table koodisto_vuosiluokat;
truncate table koodisto_koulutus cascade;

drop table if exists datantuonti_organisaatio_temp cascade;
drop table if exists datantuonti_osoite_temp cascade;