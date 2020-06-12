--select count(1) from organisaatio where maa is null or maa = '';
update organisaatio set maa = 'maatjavaltiot1_fin' where maa is null or maa = '';
