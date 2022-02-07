do
$$
    declare
        counter integer := 100000;
    begin
        while counter > 10000
            loop
                update binarydata
                set data = null
                where id in (select b.id
                             from binarydata b
                             where lo_unlink(b.data) = 1
                               and b.id in (select x.id from binarydata x where x.data is not null limit 50));
                select count(1) into counter from binarydata where data is not null;
                raise notice 'Rows left %', counter;
            end loop;
    end
$$;