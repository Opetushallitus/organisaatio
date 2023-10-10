delete
from organisaatiometadata_yhteystieto oy
using yhteystieto o
where oy.yhteystiedot_id = o.id
      and o.tyyppi = 'faksi';

delete from yhteystieto where tyyppi = 'faksi';

delete from yhteystietoelementti where tyyppi = 'Faksi';