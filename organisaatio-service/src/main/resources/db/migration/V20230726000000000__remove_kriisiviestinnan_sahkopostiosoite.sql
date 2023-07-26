CREATE TEMPORARY TABLE yhteystietojen_tyyppi_to_delete AS SELECT * FROM yhteystietojentyyppi WHERE oid = '1.2.246.562.5.31532764098';
DELETE FROM yhteystietoarvo WHERE kentta_id IN (SELECT id FROM yhteystietoelementti WHERE yhteystietojentyyppi_id IN (SELECT id FROM yhteystietojen_tyyppi_to_delete)) RETURNING *;
DELETE FROM yhteystietoelementti WHERE yhteystietojentyyppi_id IN (SELECT id FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DELETE FROM yhteystietojentyyppi_oppilaitostyypit WHERE yhteystietojentyyppi_id IN (SELECT id FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DELETE FROM yhteystietojentyyppi_organisaatiotyypit WHERE yhteystietojentyyppi_id IN (SELECT id FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DELETE FROM yhteystietojentyyppi WHERE id IN (SELECT id FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DELETE FROM monikielinenteksti_values WHERE id IN (SELECT nimi_mkt FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DELETE FROM monikielinenteksti WHERE id IN (SELECT nimi_mkt FROM yhteystietojen_tyyppi_to_delete) RETURNING *;
DROP TABLE yhteystietojen_tyyppi_to_delete;