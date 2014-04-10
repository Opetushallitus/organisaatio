update yhteystieto set wwwosoite = regexp_replace(wwwosoite,'(http://)+','http://') where wwwosoite like 'http://http://%'
