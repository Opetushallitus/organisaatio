--
-- Korvataan &amp; merkillä '&' monikielinenteksti_values taulusta.
--

update monikielinenteksti_values set value = regexp_replace(value, '&amp;', '&') where value like '%&amp;%';

