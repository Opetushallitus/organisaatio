-- Lisätään sosiaalisen median tiedoille korkeakoulujen kieliversiotuki.
-- Vaihdetaan sosiaalisen median linkeille avaimeksi juoksevan numeroinnin paikalle kielikoodi.
-- Jotta kaikki jo syötetyt tiedot voidaan säilyttää, olemassaoleva indeksinumero kopioidaan uuteen 
-- 'index'-sarakkeeseen joka myös tulee osaksi perusavainta.

ALTER TABLE monikielinenteksti_values ADD COLUMN index VARCHAR(15);
ALTER TABLE monikielinenteksti_values DROP CONSTRAINT monikielinenteksti_values_pkey;
ALTER TABLE monikielinenteksti_values ADD CONSTRAINT monikielinenteksti_values_pkey UNIQUE(id, key, index);

UPDATE monikielinenteksti_values
  SET index=substr(monikielinenteksti_values.key, 0, 15), key=regexp_replace(monikielinenteksti_values.key, '^\d{1,3}$', 'kieli_fi#1')
  FROM namedmonikielinenteksti
  WHERE namedmonikielinenteksti.value_id=monikielinenteksti_values.id AND 
    (namedmonikielinenteksti.key = 'GOOGLE_PLUS' OR namedmonikielinenteksti.key = 'FACEBOOK' OR
     namedmonikielinenteksti.key = 'LINKED_IN' OR namedmonikielinenteksti.key = 'TWITTER' OR
     namedmonikielinenteksti.key = 'MUU');
  