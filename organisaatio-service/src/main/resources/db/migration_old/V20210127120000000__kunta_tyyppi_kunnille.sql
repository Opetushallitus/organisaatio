update organisaatio
set organisaatiotyypitstr = organisaatiotyypitstr || 'organisaatiotyyppi_09|'
where
        yritysmuoto = 'Kunta' and
        organisaatiotyypitstr not like '%organisaatiotyyppi_09%';

insert into organisaatio_tyypit
select id, 'organisaatiotyyppi_09'
from organisaatio
where
        yritysmuoto = 'Kunta' and
        id not in (select organisaatio_id from organisaatio_tyypit where tyypit = 'organisaatiotyyppi_09');
