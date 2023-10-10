-- migrate all oppilaitoksenopetuskieli_3 to oppilaitoksenopetuskieli_1 and oppilaitoksenopetuskieli_2
WITH rows AS (
    insert into organisaatio_kielet (organisaatio_id, kielet)
        (select ok.organisaatio_id, 'oppilaitoksenopetuskieli_1#2'
         from organisaatio_kielet ok
         where ok.kielet like 'oppilaitoksenopetuskieli_3%'
           and not exists(select 1
                          from organisaatio_kielet x
                          where x.organisaatio_id = ok.organisaatio_id
                            and x.kielet like 'oppilaitoksenopetuskieli_1%'))
        returning 1
)
SELECT CONCAT(count(*), ' oppilaitoksenopetuskieli_1 inserted ')
from rows;
WITH rows AS (
    insert into organisaatio_kielet (organisaatio_id, kielet)
        (select ok.organisaatio_id, 'oppilaitoksenopetuskieli_2#2'
         from organisaatio_kielet ok
         where ok.kielet like 'oppilaitoksenopetuskieli_3%'
           and not exists(select 1
                          from organisaatio_kielet x
                          where x.organisaatio_id = ok.organisaatio_id
                            and x.kielet like 'oppilaitoksenopetuskieli_2%'))
        returning 1
)
SELECT CONCAT(count(*), ' oppilaitoksenopetuskieli_2 inserted ')
from rows;
WITH rows AS (
    delete
        from organisaatio_kielet ok
            where ok.kielet like 'oppilaitoksenopetuskieli_3%'
            returning 1
)
SELECT CONCAT(count(*), ' oppilaitoksenopetuskieli_3 deleted ')
from rows;