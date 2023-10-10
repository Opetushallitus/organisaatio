ALTER TABLE yhteystieto ADD COLUMN kieli varchar(255);
update yhteystieto set kieli = 'kielivalikoima_fi' where osoitetyyppi is null or osoitetyyppi = '';
update yhteystieto set kieli = 'kielivalikoima_fi' where osoitetyyppi = 'posti';
update yhteystieto set kieli = 'kielivalikoima_fi' where osoitetyyppi = 'kaynti';
update yhteystieto set kieli = 'kielivalikoima_sv' where osoitetyyppi = 'ruotsi_posti';
update yhteystieto set kieli = 'kielivalikoima_sv' where osoitetyyppi = 'ruotsi_kaynti';
update yhteystieto set kieli = 'kielivalikoima_en' where osoitetyyppi = 'ulkomainen_posti';
update yhteystieto set kieli = 'kielivalikoima_en' where osoitetyyppi = 'ulkomainen_kaynti';