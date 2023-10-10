--
-- Muutetaan www-osoitteet www.koulu.fi muotoon http://www.koulu.fi
--

update yhteystieto set wwwosoite = regexp_replace(wwwosoite, '^www.', 'http://www.') where wwwosoite like 'www%';

