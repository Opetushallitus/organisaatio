update monikielinenteksti_values set key = replace(key, 'kielivalikoima_', 'kieli_') where key like 'kieli_%';
update monikielinenteksti_values set key = concat(key, '#1') where key like 'kieli_%';
