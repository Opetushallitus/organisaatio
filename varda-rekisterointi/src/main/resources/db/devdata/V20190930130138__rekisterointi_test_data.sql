INSERT INTO rekisterointi (id, organisaatio, toimintamuoto, kunnat, sahkopostit)
 VALUES (
     1,
     '{"oid":null,"ytunnus":"0000000-0","nimi":{"fi":"Testi Yritys"},"nimet":[{"nimi":{"fi":"Testi Yritys Oy","se":"Test Bolag Ab"}}],"alkuPvm":null}'::jsonb,
     'perhepäivähoitaja',
     '{"Helsinki"}',
     '{"testi.yritys@testiyrit.ys"}'
 );

INSERT INTO kayttaja (id, etunimi, sukunimi, sahkoposti, asiointikieli, saateteksti, rekisterointi)
 VALUES (
     1,
     'Testi',
     'Käyttäjä',
     'testi.kayttaja@testiyrit.ys',
     'fi',
     null,
     1
 );
