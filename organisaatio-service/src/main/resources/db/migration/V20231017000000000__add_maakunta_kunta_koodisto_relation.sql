CREATE TABLE koodisto_maakunta (
    koodiuri text PRIMARY KEY,
    koodiarvo text NOT NULL,
    versio bigint NOT NULL,
    nimi_fi text,
    nimi_sv text,
    UNIQUE (koodiarvo)
);

CREATE TABLE maakuntakuntarelation (
    kuntauri text NOT NULL REFERENCES koodisto_kunta(koodiuri),
    maakuntauri text NOT NULL REFERENCES koodisto_maakunta(koodiuri),
    PRIMARY KEY (maakuntauri, kuntauri)
);

INSERT INTO koodisto_maakunta(koodiuri, koodiarvo, versio, nimi_fi, nimi_sv) VALUES
    ('maakunta_01', '01', 2, 'Uusimaa', 'Nyland'),
    ('maakunta_02', '02', 2, 'Varsinais-Suomi', 'Egentliga Finland'),
    ('maakunta_04', '04', 2, 'Satakunta', 'Satakunta'),
    ('maakunta_05', '05', 2, 'Kanta-Häme', 'Egentliga Tavastland'),
    ('maakunta_06', '06', 2, 'Pirkanmaa', 'Birkaland'),
    ('maakunta_07', '07', 2, 'Päijät-Häme', 'Päijänne-Tavastland'),
    ('maakunta_08', '08', 2, 'Kymenlaakso', 'Kymmenedalen'),
    ('maakunta_09', '09', 2, 'Etelä-Karjala', 'Södra Karelen'),
    ('maakunta_10', '10', 2, 'Etelä-Savo', 'Södra Savolax'),
    ('maakunta_11', '11', 2, 'Pohjois-Savo', 'Norra Savolax'),
    ('maakunta_12', '12', 2, 'Pohjois-Karjala', 'Norra Karelen'),
    ('maakunta_13', '13', 2, 'Keski-Suomi', 'Mellersta Finland'),
    ('maakunta_14', '14', 2, 'Etelä-Pohjanmaa', 'Södra Österbotten'),
    ('maakunta_15', '15', 2, 'Pohjanmaa', 'Österbotten'),
    ('maakunta_16', '16', 2, 'Keski-Pohjanmaa', 'Mellersta Österbotten'),
    ('maakunta_17', '17', 2, 'Pohjois-Pohjanmaa', 'Norra Österbotten'),
    ('maakunta_18', '18', 2, 'Kainuu', 'Kajanaland'),
    ('maakunta_19', '19', 2, 'Lappi', 'Lappland'),
    ('maakunta_20', '20', 2, 'Itä-Uusimaa', 'Östra Nyland'),
    ('maakunta_21', '21', 2, 'Ahvenanmaa', 'Åland'),
    ('maakunta_99', '99', 2, 'Ei tiedossa (maakunta)', 'Okänt landskap');