--
-- Korjataan virheellisiä kielikoodiston ureja (OVT-8645)
--

update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_1#1' where kielet = 'kieli_fi';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_9#1' where kielet = 'kielivalikoima_99';
delete from organisaatio_kielet where kielet = 'kielivalikoima_fr';
delete from organisaatio_kielet where kielet = 'kielivalikoima_ru';
delete from organisaatio_kielet where kielet = 'kielivalikoima_de';
delete from organisaatio_kielet where kielet = 'kielivalikoima_ja';
delete from organisaatio_kielet where kielet = 'kielivalikoima_es';
delete from organisaatio_kielet where kielet = 'kielivalikoima_fr';
delete from organisaatio_kielet where kielet = 'kielivalikoima_fr';
delete from organisaatio_kielet where kielet = 'kielivalikoima_fr';
