select count(distinct mktv.id || mktv.key) as before
from organisaatio_nimi onimi
         join organisaatio o1
              on onimi.organisaatio_id = o1.id --and o1.organisaatiopoistettu = false --and o1.lakkautuspvm is null
         join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
         join monikielinenteksti mkt on onimi.nimi_mkt = mkt.id
         join monikielinenteksti_values mktv on mkt.id = mktv.id
where mktv.value like '%, %';
WITH rows AS (
    update monikielinenteksti_values
        set value = trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2))
        from (select distinct mktv.id                 as id,
                              mktv.key                as key,
                              mktv.value              as toimipistenimi,
                              case
                                  when position(mktv2.value || ', ' in mktv.value) = 1 then mktv2.value
                                  when position(mktv3.value || ', ' in mktv.value) = 1 then mktv3.value
                                  when position(mktv4.value || ', ' in mktv.value) = 1 then mktv4.value
                                  else mktv.value end as oppilaitosnimi
              from monikielinenteksti_values mktv
                       join monikielinenteksti mkt on mkt.id = mktv.id
                       join organisaatio_nimi onimi on mkt.id = onimi.nimi_mkt
                       join organisaatio o1 on onimi.organisaatio_id = o1.id
                       join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'

                       join organisaatiosuhde suhde on o1.id = suhde.child_id and suhde.suhdetyyppi = 'HISTORIA'
                       join organisaatiosuhde suhde2
                            on suhde2.child_id = suhde.parent_id and suhde2.suhdetyyppi = 'HISTORIA'
                       join organisaatio_parent_oids parents on o1.id = parents.organisaatio_id

                       join organisaatio o2 on suhde.parent_id = o2.id
                       join organisaatio_nimi nimi2 on nimi2.organisaatio_id = o2.id
                       join monikielinenteksti mkt2 on nimi2.nimi_mkt = mkt2.id
                       join monikielinenteksti_values mktv2 on mkt2.id = mktv2.id-- and mktv.key = mktv2.key

                       join organisaatio o3 on parents.parent_oid = o3.oid
                       join organisaatio_nimi nimi3 on nimi3.organisaatio_id = o3.id
                       join monikielinenteksti mkt3 on nimi3.nimi_mkt = mkt3.id
                       join monikielinenteksti_values mktv3 on mkt3.id = mktv3.id --and mktv.key = mktv3.key

                       join organisaatio o4 on suhde2.parent_id = o4.id
                       join organisaatio_nimi nimi4 on nimi4.organisaatio_id = o4.id
                       join monikielinenteksti mkt4 on nimi4.nimi_mkt = mkt4.id
                       join monikielinenteksti_values mktv4 on mkt4.id = mktv4.id-- and mktv.key = mktv4.key
              where mktv.value like mktv2.value || ', %'
                 or mktv.value like mktv3.value || ', %'
                 or mktv.value like mktv4.value || ', %') as sub
            join monikielinenteksti_values mktv on mktv.key = sub.key and mktv.id = sub.id
        where monikielinenteksti_values.id = mktv.id
            and monikielinenteksti_values.key = mktv.key and mktv.value != sub.oppilaitosnimi returning sub.id, sub.key, sub.oppilaitosnimi, sub.toimipistenimi, trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2)) as uusi_nimi)
SELECT *
from rows;

select count(distinct mktv.id || mktv.key) as between_runs
from organisaatio_nimi onimi
         join organisaatio o1
              on onimi.organisaatio_id = o1.id --and o1.organisaatiopoistettu = false --and o1.lakkautuspvm is null
         join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
         join monikielinenteksti mkt on onimi.nimi_mkt = mkt.id
         join monikielinenteksti_values mktv on mkt.id = mktv.id
where mktv.value like '%, %';

WITH rows AS (
    update monikielinenteksti_values
        set value = trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2))
        from (select distinct mktv.id                 as id,
                              mktv.key                as key,
                              mktv.value              as toimipistenimi,
                              case
                                  when position(mktv2.value || ', ' in mktv.value) = 1 then mktv2.value
                                  when position(mktv3.value || ', ' in mktv.value) = 1 then mktv3.value
                                  when position(mktv4.value || ', ' in mktv.value) = 1 then mktv4.value
                                  else mktv.value end as oppilaitosnimi
              from monikielinenteksti_values mktv
                       join monikielinenteksti mkt on mkt.id = mktv.id
                       join organisaatio_nimi onimi on mkt.id = onimi.nimi_mkt
                       join organisaatio o1 on onimi.organisaatio_id = o1.id
                       join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'

                       join organisaatiosuhde suhde on o1.id = suhde.child_id and suhde.suhdetyyppi = 'HISTORIA'
                       join organisaatiosuhde suhde2
                            on suhde2.child_id = suhde.parent_id and suhde2.suhdetyyppi = 'HISTORIA'
                       join organisaatio_parent_oids parents on o1.id = parents.organisaatio_id

                       join organisaatio o2 on suhde.parent_id = o2.id
                       join organisaatio_nimi nimi2 on nimi2.organisaatio_id = o2.id
                       join monikielinenteksti mkt2 on nimi2.nimi_mkt = mkt2.id
                       join monikielinenteksti_values mktv2 on mkt2.id = mktv2.id --and mktv.key = mktv2.key

                       join organisaatio o3 on parents.parent_oid = o3.oid
                       join organisaatio_nimi nimi3 on nimi3.organisaatio_id = o3.id
                       join monikielinenteksti mkt3 on nimi3.nimi_mkt = mkt3.id
                       join monikielinenteksti_values mktv3 on mkt3.id = mktv3.id --and mktv.key = mktv3.key

                       join organisaatio o4 on suhde2.parent_id = o4.id
                       join organisaatio_nimi nimi4 on nimi4.organisaatio_id = o4.id
                       join monikielinenteksti mkt4 on nimi4.nimi_mkt = mkt4.id
                       join monikielinenteksti_values mktv4 on mkt4.id = mktv4.id --and mktv.key = mktv4.key
              where mktv.value like mktv2.value || ', %'
                 or mktv.value like mktv3.value || ', %'
                 or mktv.value like mktv4.value || ', %') as sub
            join monikielinenteksti_values mktv on mktv.key = sub.key and mktv.id = sub.id
        where monikielinenteksti_values.id = mktv.id
            and monikielinenteksti_values.key = mktv.key and mktv.value != sub.oppilaitosnimi returning sub.id, sub.key, sub.oppilaitosnimi, sub.toimipistenimi, trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2)) as uusi_nimi)
SELECT *
from rows;

select count(distinct mktv.id || mktv.key) as after
from organisaatio_nimi onimi
         join organisaatio o1
              on onimi.organisaatio_id = o1.id --and o1.organisaatiopoistettu = false and o1.lakkautuspvm is null
         join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
         join monikielinenteksti mkt on onimi.nimi_mkt = mkt.id
         join monikielinenteksti_values mktv on mkt.id = mktv.id
where mktv.value like '%, %';

WITH rows AS (
    update monikielinenteksti_values
        set value = trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2))
        from (select distinct mktv.id                 as id,
                              mktv.key                as key,
                              mktv.value              as toimipistenimi,
                              case
                                  when position(mktv2.value || ', ' in mktv.value) = 1 then mktv2.value
                                  when position(mktv3.value || ', ' in mktv.value) = 1 then mktv3.value
                                  when position(mktv4.value || ', ' in mktv.value) = 1 then mktv4.value
                                  else mktv.value end as oppilaitosnimi
              from monikielinenteksti_values mktv
                       join monikielinenteksti mkt on mkt.id = mktv.id
                       join organisaatio_nimi onimi on mkt.id = onimi.nimi_mkt
                       join organisaatio o1 on onimi.organisaatio_id = o1.id
                       join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'

                       join organisaatiosuhde suhde on o1.id = suhde.child_id and suhde.suhdetyyppi = 'HISTORIA'
                       join organisaatiosuhde suhde2
                            on suhde2.child_id = suhde.parent_id and suhde2.suhdetyyppi = 'HISTORIA'
                       join organisaatio_parent_oids parents on o1.id = parents.organisaatio_id

                       join organisaatio o2 on suhde.parent_id = o2.id
                       join organisaatio_nimi nimi2 on nimi2.organisaatio_id = o2.id
                       join monikielinenteksti mkt2 on nimi2.nimi_mkt = mkt2.id
                       join monikielinenteksti_values mktv2 on mkt2.id = mktv2.id --and mktv.key = mktv2.key

                       join organisaatio o3 on parents.parent_oid = o3.oid
                       join organisaatio_nimi nimi3 on nimi3.organisaatio_id = o3.id
                       join monikielinenteksti mkt3 on nimi3.nimi_mkt = mkt3.id
                       join monikielinenteksti_values mktv3 on mkt3.id = mktv3.id --and mktv.key = mktv3.key

                       join organisaatio o4 on suhde2.parent_id = o4.id
                       join organisaatio_nimi nimi4 on nimi4.organisaatio_id = o4.id
                       join monikielinenteksti mkt4 on nimi4.nimi_mkt = mkt4.id
                       join monikielinenteksti_values mktv4 on mkt4.id = mktv4.id --and mktv.key = mktv4.key
              where mktv.value like mktv2.value || ', %'
                 or mktv.value like mktv3.value || ', %'
                 or mktv.value like mktv4.value || ', %') as sub
            join monikielinenteksti_values mktv on mktv.key = sub.key and mktv.id = sub.id
        where monikielinenteksti_values.id = mktv.id
            and monikielinenteksti_values.key = mktv.key and mktv.value != sub.oppilaitosnimi returning sub.id, sub.key, sub.oppilaitosnimi, sub.toimipistenimi, trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2)) as uusi_nimi)
SELECT *
from rows;

select count(distinct mktv.id || mktv.key) as after_after
from organisaatio_nimi onimi
         join organisaatio o1
              on onimi.organisaatio_id = o1.id --and o1.organisaatiopoistettu = false and o1.lakkautuspvm is null
         join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
         join monikielinenteksti mkt on onimi.nimi_mkt = mkt.id
         join monikielinenteksti_values mktv on mkt.id = mktv.id
where mktv.value like '%, %';