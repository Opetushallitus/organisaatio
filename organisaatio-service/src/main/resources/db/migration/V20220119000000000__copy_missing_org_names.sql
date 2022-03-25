CREATE OR REPLACE FUNCTION migrate_organisaatio_names(_to varchar, _from varchar, OUT _rows varchar)
    RETURNS varchar AS
$$
BEGIN
    WITH rows AS (
        insert into monikielinenteksti_values (id, value, key, index)
            (select mv2.id, mv2.value, _to, 0
             from organisaatio o
                      join monikielinenteksti m on o.nimi_mkt = m.id
                      left join monikielinenteksti_values mv1 on m.id = mv1.id and mv1.key = _to
                      join monikielinenteksti_values mv2 on m.id = mv2.id and mv2.key = _from
             where mv1.id is null)
            RETURNING 1
    )
    SELECT CONCAT(count(*), ' names inserted ')
    INTO _rows
    FROM rows;
END;
$$ LANGUAGE plpgsql;
SELECT count(*), ' before delete'
from monikielinenteksti_values mv
where exists(select 1
             from organisaatio o
                      join monikielinenteksti m on m.id = o.nimi_mkt
             where m.id = mv.id);
delete
from monikielinenteksti_values mv
where TRIM(mv.value) = ''
   or mv.value is null and exists(select 1
                                  from organisaatio o
                                           join monikielinenteksti m on m.id = o.nimi_mkt
                                  where m.id = mv.id);
SELECT count(*), ' after delete'
from monikielinenteksti_values mv
where exists(select 1
             from organisaatio o
                      join monikielinenteksti m on m.id = o.nimi_mkt
             where m.id = mv.id);
SELECT migrate_organisaatio_names('fi', 'sv'), ': sv --> fi';
SELECT migrate_organisaatio_names('fi', 'en'), ': en --> fi';
SELECT migrate_organisaatio_names('sv', 'fi'), ': fi --> sv';
SELECT migrate_organisaatio_names('en', 'fi'), ': fi --> en';
SELECT count(*), ' after migration'
from monikielinenteksti_values mv
where exists(select 1
             from organisaatio o
                      join monikielinenteksti m on m.id = o.nimi_mkt
             where m.id = mv.id);