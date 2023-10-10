update yhteystieto set kieli = replace(kieli, 'kielivalikoima_', 'kieli_') where kieli like 'kielivalikoima_%';
update yhteystieto set kieli = concat(kieli, '#1') where kieli like 'kieli_%';
