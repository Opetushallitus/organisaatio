CREATE TABLE koodisto_vuosiluokat (
    koodiuri text PRIMARY KEY,
    koodiarvo text NOT NULL,
    versio bigint NOT NULL,
    nimi_fi text,
    nimi_sv text,
    UNIQUE (koodiarvo)
);

INSERT INTO koodisto_vuosiluokat (koodiuri, koodiarvo, versio, nimi_fi, nimi_sv) VALUES
    ('vuosiluokat_1', '1', 2, '1. vuosiluokalla', '1. årsklass'),
    ('vuosiluokat_10', '10', 2, 'Lisäopetuksessa', 'Påbyggnadsundervisning'),
    ('vuosiluokat_11', '11', 2, 'Esiopetuksessa', 'Förundervisning'),
    ('vuosiluokat_12', '12', 2, 'Aamu- ja/tai iltapäivätoiminnassa', 'Morgon- och eftermiddagsverksamhet'),
    ('vuosiluokat_2', '2', 2, '2. vuosiluokalla', '2. årsklass'),
    ('vuosiluokat_3', '3', 2, '3. vuosiluokalla', '3. årsklass'),
    ('vuosiluokat_4', '4', 2, '4. vuosiluokalla', '4. årsklass'),
    ('vuosiluokat_5', '5', 2, '5. vuosiluokalla', '5. årsklass'),
    ('vuosiluokat_6', '6', 2, '6. vuosiluokalla', '6. årsklass'),
    ('vuosiluokat_7', '7', 2, '7. vuosiluokalla', '7. årsklass'),
    ('vuosiluokat_8', '8', 2, '8. vuosiluokalla', '8. årsklass'),
    ('vuosiluokat_9', '9', 2, '9. vuosiluokalla', '9. årsklass');