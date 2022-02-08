# Organisaatioiden hallintapalvelu

Sisältää sovellukset

    Organisaatiopalvelun REST- ja SOAP-palvelut
        organisaatio-service
    Käyttöliittymä
        organisaatio
    Mock virkailija
        mock-api

## Swagger-dokumentaatio

https://virkailija.opintopolku.fi/organisaatio/swagger-ui.html

## Sovelluksen pystyttäminen ja ajaminen omalla koneella

### Koonti:

Aja projektin juuressa

```bash
   .\gradlew clean build
```

### Testit

Koonnissa ajetaan yksikkötestit, linttaus ja npm audit. CI-palvelimella ajetaan tämän lisäksi vielä integraatiotestit käyttäen Cypress:io:ta.

#### Integraatiotestaus

Ci:ssä testit ajetaan käytännössä näin:

```bash
    ./gradlew build -x test
    psql -c 'CREATE DATABASE organisaatio;' -U postgres
    FLYWAY_USER=postgres ./gradlew flywayMigrate
    npm run mock-api
    java -jar -Dspring.config.location=classpath:application.properties -Dspring.profiles.active=dev ./organisaatio-service/build/libs/organisaatio-service.jar &
    npm run cypress:ci
```

Eli build -> tietokanta ja sen alustus -> mock api -> java -> cypress. kts Travis.yml

### Paikallinen tietokanta

Paikallinen kehitys nojaa paikallisesti asennettuun tietokantaan. Aja sopiva postgres dockerissa esimeskiksi seuraavalla composella:

```
version: '3'
services:
  database:
    container_name: oph-postgres-db
    image: postgres:13.4
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
      - POSTGRES_HOST_AUTH_METHOD=trust
    volumes:
      - database-data:/var/lib/postgresql/data/
      - ./backup:/tmp/backup
    ports:
      - 5432:5432
volumes:
  database-data:
```

Tuo backup pallero ympäristöstä tai luo tyhjä kanta organisaatio sovellusta varten. Lataa backup S3:sta ja tallenna composen viereen /backup hakemistoon ja backupin palautus onnistuu:

```
docker exec oph-postgres-db dropdb -U postgres organisaatio
docker exec oph-postgres-db createdb -U postgres -T template0 organisaatio
docker exec oph-postgres-db pg_restore -U postgres -d organisaatio /tmp/backup/organisaatio.backup
```

TAI alusta tyhja kanta ja aja flyway clean ja flyway migrate. Tämän jälkeen ohjelmiston voi käynnistää joko jar:sta tai esim Idean Spring boot konfiguraatiolla.

```
docker exec oph-postgres-db createdb -U postgres -T template0 organisaatio
./gradlew flywayClean flywayMigrate

```

### Käynnistäminen

#### Virkailija-mock

Lokaalikehitystä varten voit käynnistää mock-api moduulin.
Asenna ensin riippuvuudet ajamalla mock-api hakemistossa ensin

```bash
npm install
```

käynnistä sitten mock-api ajamalla:

```bash
npm run mock-api
```

ui käynnistyy porttiin 9000. Avaa http://localhost:9000/kayttooikeus-service/cas/me selaimessa testataksesi vakiovastausta.

#### Backend

Käynnistä backend jar:sta tai spring boot configuraatiolla ideasta.

Käynnistys Jar:sta juuressa (Ui vastaa myös osoitteesta http://localhost:8080/organisaatio).

```bash
java -jar -Dspring.config.location=classpath:application.properties -Dspring.profiles.active=dev  -Durl-virkailija=http://localhost:9000 -Dhost.virkailija=localhost:9000 ./organisaatio-service/build/libs/organisaatio-service.jar
```

#### Frontend

Asenna ensin riippuvuudet ajamalla organisaatio-ui hakemistossa ensin

```bash
npm install
```

käynnistä sitten ui ajamalla:

```bash
npm run start
```

Ui käynnistyy porttiin 3003. Avaa http://localhost:3003/organisaatio selaimessa.

### Profiilit

Dev-profiili käyttää http basic authentikaatiota tunnuksilla devaaja/devaaja. KTS. fi.vm.sade.organisaatio.config.DevUserDetailsServiceConfiguration.

## IDEA Kehitys

Git repoon on tallennettu .run hakemistoon valmiita ajokonfigurointeja backend+mockapi ja frontend+javascript-debug ajoja varten.

## Opetushallituksen palvelukokonaisuus

https://confluence.csc.fi/display/OPHPALV/Opetushallituksen+palvelukokonaisuus
