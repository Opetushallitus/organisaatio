CREATE OR REPLACE PROCEDURE remove_binarydata()
    LANGUAGE plpgsql
AS
$$
BEGIN
    update binarydata
    set data = null
    where id in (select b.id
                 from binarydata b
                 where lo_unlink(b.data) = 1
                   and b.id in (select x.id from binarydata x where x.data is not null limit 50));
END;
$$;

select 'initial lobs size', count(1)
from (select distinct loid from pg_largeobject) as loids;
do
$$
    declare
        counter integer := 100000;
    begin
        while counter > 10000
            loop
                call remove_binarydata();
                select count(1) into counter from binarydata where data is not null;
                raise notice 'Rows left %', counter;
            end loop;
    end
$$;

select 'lobs after unlinking', count(1)
from (select distinct loid from pg_largeobject) as loids;