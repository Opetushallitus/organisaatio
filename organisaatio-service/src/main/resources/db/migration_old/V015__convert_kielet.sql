--select replace(kielet, 'kieli_', 'kielivalikoima_') from organisaatio_kielet where kielet like 'kieli_%';
update organisaatio_kielet set kielet = replace(kielet, 'kieli_', 'kielivalikoima_') where kielet like 'kieli_%';