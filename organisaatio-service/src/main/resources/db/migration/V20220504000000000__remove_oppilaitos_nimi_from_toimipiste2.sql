update monikielinenteksti_values
set value = trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2))
from (select distinct mktv.id as id, mktv.key as key, mktv3.value || ', ' || mktv2.value as oppilaitosnimi
      from monikielinenteksti_values mktv
               join monikielinenteksti mkt on mkt.id = mktv.id
               join organisaatio_nimi onimi on mkt.id = onimi.nimi_mkt
               join organisaatio o1 on onimi.organisaatio_id = o1.id
               join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
               join organisaatiosuhde opo on o1.id = opo.child_id
               join organisaatio o2 on opo.parent_id = o2.id
               join organisaatio_nimi nimi2 on nimi2.organisaatio_id = o2.id
               join monikielinenteksti mkt2 on nimi2.nimi_mkt = mkt2.id
               join monikielinenteksti_values mktv2
                    on mkt2.id = mktv2.id and mktv.key = mktv2.key
               join organisaatiosuhde opo2 on o2.id = opo2.child_id
               join organisaatio o3 on opo2.parent_id = o3.id
               join organisaatio_nimi nimi3 on nimi3.organisaatio_id = o3.id
               join monikielinenteksti mkt3 on nimi3.nimi_mkt = mkt3.id
               join monikielinenteksti_values mktv3 on mkt3.id = mktv3.id and mktv.key = mktv3.key
      where mktv.value like mktv3.value || ', ' || mktv2.value || ', %') as sub
         join monikielinenteksti_values mktv on mktv.key = sub.key and mktv.id = sub.id
where monikielinenteksti_values.id = mktv.id
  and monikielinenteksti_values.key = mktv.key;


update monikielinenteksti_values
set value = trim(substring(mktv.value from length(sub.oppilaitosnimi) + 2))
from (select distinct mktv.id as id, mktv.key as key, mktv2.value as oppilaitosnimi
      from monikielinenteksti_values mktv
               join monikielinenteksti mkt on mkt.id = mktv.id
               join organisaatio_nimi onimi on mkt.id = onimi.nimi_mkt
               join organisaatio o1 on onimi.organisaatio_id = o1.id
               join organisaatio_tyypit ot on o1.id = ot.organisaatio_id and ot.tyypit = 'organisaatiotyyppi_03'
               join organisaatiosuhde opo on o1.id = opo.child_id
               join organisaatio o2 on opo.parent_id = o2.id
               join organisaatio_nimi nimi2 on nimi2.organisaatio_id = o2.id
               join monikielinenteksti mkt2 on nimi2.nimi_mkt = mkt2.id
               join monikielinenteksti_values mktv2
                    on mkt2.id = mktv2.id and mktv.key = mktv2.key and mktv.value like mktv2.value || ', %') as sub
         join monikielinenteksti_values mktv on mktv.key = sub.key and mktv.id = sub.id
where monikielinenteksti_values.id = mktv.id
  and monikielinenteksti_values.key = mktv.key;