
CREATE FUNCTION organisaatioSuhdeInsert() RETURNS void AS 
$$
DECLARE
    o organisaatio%ROWTYPE;


    _id bigint;
    _suhdeTyyppi varchar(255) = 'HISTORIA';
    _parentId bigint;
    _childId bigint;

BEGIN

FOR o IN
SELECT * 
    FROM organisaatio
    WHERE NOT(organisaatio.parent_id is null)

LOOP

    _id = nextval('public.hibernate_sequence');
        _parentId = o.parent_id;
    _childId = o.id;

insert into organisaatiosuhde (id, version, alkuPvm, loppuPvm, suhdetyyppi, parent_id, child_id) 
values (_id, 0, CURRENT_TIMESTAMP, null, _suhdeTyyppi,_parentId, _childId);

END LOOP;

END;

$$ LANGUAGE plpgsql;

SELECT organisaatioSuhdeInsert();

