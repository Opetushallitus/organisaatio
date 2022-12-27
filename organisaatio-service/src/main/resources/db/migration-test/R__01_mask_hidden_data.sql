with input as (select o.oid,
                      o.ytunnus,
                      o.id as organisaatio_id,
                      mv.id as nimi_id,
                      mv2.id as nimet_id,
                      y.id as yhteystieto_id
               from organisaatio o
                        left join monikielinenteksti m on o.nimi_mkt = m.id
                        left join monikielinenteksti_values mv on m.id = mv.id
                        left join organisaatio_nimi n on o.id = n.organisaatio_id
                        left join monikielinenteksti m2 on n.nimi_mkt = m2.id
                        left join monikielinenteksti_values mv2 on m2.id = mv2.id
                        left join yhteystieto y on o.id = y.organisaatio_id
               where o.yritysmuoto = 'Yksityinen elinkeinonharjoittaja' or o.piilotettu = true)
   ,yhteystiedotupdated as (update yhteystieto set
    osoite = 'Testiosoite',
    email='test@test.fi',
    postinumero = 'posti_99999',
    postitoimipaikka = 'Digitointi',
    puhelinnumero='+358123456789'
    where yhteystieto.id in (select distinct yhteystieto_id from input) returning *)
   ,nimetupdated1 as (update monikielinenteksti_values set value = coalesce('ytunnus ' || x.ytunnus,'oid ' || x.oid) from input x where x.nimi_id = monikielinenteksti_values.id returning *)
   ,nimetupdated2 as (update monikielinenteksti_values set value = coalesce('ytunnus ' || x.ytunnus,'oid ' || x.oid) from input x where x.nimet_id = monikielinenteksti_values.id returning *)
   ,nimihaku as (update organisaatio set nimihaku = coalesce('ytunnus ' || x.ytunnus,'oid ' || x.oid) from input x where x.organisaatio_id = organisaatio.id returning *)
select count(*) from input;