CREATE OR REPLACE FUNCTION migrate_organisaatio_names(varchar, varchar) RETURNS integer
AS
$$
BEGIN
    insert into monikielinenteksti_values (id, value, key, index)
        (select mv2.id, mv2.value, $1, 0
         from organisaatio o
                  join monikielinenteksti m on o.nimi_mkt = m.id
                  left join monikielinenteksti_values mv1 on m.id = mv1.id and mv1.key = $1
                  join monikielinenteksti_values mv2 on m.id = mv2.id and mv2.key = $2
         where mv1.id is null);
    RETURN 1;
END;
$$ LANGUAGE plpgsql;

delete
from monikielinenteksti_values mv
where TRIM(mv.value) = ''
   or mv.value is null and exists(select 1
                                  from organisaatio o
                                           join monikielinenteksti m on m.id = o.nimi_mkt
                                  where m.id = mv.id);
SELECT migrate_organisaatio_names('fi', 'sv'); -- sv --> fi
SELECT migrate_organisaatio_names('fi', 'en'); -- en --> fi
SELECT migrate_organisaatio_names('sv', 'fi'); -- fi --> sv
SELECT migrate_organisaatio_names('en', 'fi'); -- fi --> en
DROP FUNCTION migrate_organisaatio_names(varchar, varchar);