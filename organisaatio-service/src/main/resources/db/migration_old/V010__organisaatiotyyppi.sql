ALTER TABLE organisaatio ADD COLUMN organisaatiotyypitstr varchar(255);

CREATE FUNCTION organisaatiotyypitstrInsert() RETURNS void AS 
$$
DECLARE
    o organisaatio%ROWTYPE;
    ot organisaatio_tyypit%ROWTYPE;

    _tyypitstr varchar(255);
    

BEGIN

FOR o IN
SELECT * 
    FROM organisaatio

LOOP

	_tyypitstr = '';
	
	FOR ot IN
	SELECT *
		FROM organisaatio_tyypit
		WHERE organisaatio_tyypit.organisaatio_id = o.id
	
	LOOP
		
	_tyypitstr = _tyypitstr || ot.tyypit || '|';	
	
	END LOOP;

UPDATE organisaatio SET organisaatiotyypitstr = _tyypitstr WHERE id = o.id;

END LOOP;

END;

$$ LANGUAGE plpgsql;

SELECT organisaatiotyypitstrInsert();