## Yleistä
organisaatio-ui on organisaatiopalvelun käyttöliittymän toteuttava moduli.

Organisaatiotiedot haetaan organisaatiopalvelun REST-rajapinnasta. Rajapinnassa data välitetään DTO-olioina.

Moduuli käyttää lisäksi autentikointipalvelun, koodistopalvelun ja lokalisointipalvelun rajapintoja.

## Kehitys
VM parametreihin voi lisätä `-Dorganisaatio-service.url.rest=http://localhost:8180/organisaatio-service/rest/` niin UI kutsuu lokaalia organisaatiopalvelua testiympäristön sijasta.
