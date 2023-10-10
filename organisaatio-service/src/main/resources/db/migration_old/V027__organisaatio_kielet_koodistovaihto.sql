update organisaatio_kielet set kielet =  concat(kielet, '#1') where kielet like 'oppilaitoksenopetuskieli_%';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_1#1' where kielet = 'kielivalikoima_fi';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_2#1' where kielet = 'kielivalikoima_sv';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_4#1' where kielet = 'kielivalikoima_en';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_5#1' where kielet = 'kielivalikoima_se';
update organisaatio_kielet set kielet = 'oppilaitoksenopetuskieli_9#1' where kielet = 'kielivalikoima_%';
