update monikielinenteksti_values
set value = (
    select case
               when position(pmv.value || ', ' in mv.value) = 1 then trim(both ' ' from
                                                                          substring(mv.value from char_length(pmv.value) + 3))
               else mv.value end
    from organisaatio o
             join monikielinenteksti m on o.nimi_mkt = m.id
             join monikielinenteksti_values mv on m.id = mv.id
             JOIN organisaatio_parent_oids pp ON (pp.organisaatio_id = o.id) and parent_position = 0
             JOIN organisaatio p on pp.parent_oid = p.oid
             JOIN monikielinenteksti pm on p.nimi_mkt = pm.id
             JOIN monikielinenteksti_values pmv on pm.id = pmv.id and mv.key = pmv.key
    where monikielinenteksti_values.id = mv.id
      and monikielinenteksti_values.key = mv.key
)
where id in (select mv.id
             from organisaatio o
                      join organisaatio_tyypit ot on o.id = ot.organisaatio_id
                      join monikielinenteksti m on o.nimi_mkt = m.id
                      join monikielinenteksti_values mv on m.id = mv.id
                      JOIN organisaatio_parent_oids pp ON (pp.organisaatio_id = o.id) and parent_position = 0
                      JOIN organisaatio p on pp.parent_oid = p.oid
                      JOIN monikielinenteksti pm on p.nimi_mkt = pm.id
                      JOIN monikielinenteksti_values pmv on pm.id = pmv.id and mv.key = pmv.key
             where ot.tyypit = 'organisaatiotyyppi_03'
               and (o.lakkautuspvm is null or o.lakkautuspvm > current_date)
               and o.organisaatiopoistettu = false
               and o.alkupvm < CURRENT_DATE
               and position(pmv.value || ', ' in mv.value) = 1);