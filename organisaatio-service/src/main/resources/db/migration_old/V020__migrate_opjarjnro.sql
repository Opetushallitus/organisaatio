CREATE FUNCTION opjarjnroMigrate() RETURNS void AS 
$$
DECLARE
    o organisaatio%ROWTYPE;

    _opjarjnro varchar(255);

BEGIN

FOR o IN
SELECT * 
    FROM organisaatio

LOOP

    _opjarjnro = o.opetuspisteenJarjNro;
    

 UPDATE organisaatiosuhde SET opetuspisteenJarjNro = _opjarjnro WHERE child_id = o.id;

END LOOP;

END;

$$ LANGUAGE plpgsql;

SELECT opjarjnroMigrate();
