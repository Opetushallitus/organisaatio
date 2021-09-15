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

``` bash
   .\gradlew clean build
```
### Käynnistäminen
#### Virkailija-mock
Lokaalikehitystä varten voit käynnistää mock-api moduulin.
Asenna ensin riippuvuudet ajamalla mock-api hakemistossa ensin
``` bash
npm install
```
käynnistä sitten mock-api ajamalla:
``` bash
npm run start
```
ui käynnistyy porttiin 9000. Avaa http://localhost:9000/kayttooikeus-service/cas/me selaimessa.
#### Backend
Käynnistä backend jar:sta tai spring boot configuraatiolla ideasta.

Käynnistys Jar:sta juuressa.
``` bash
java -jar -Dspring.config.location=classpath:application.properties,classpath:dev.properties -Dspring.profiles.active=dev ./organisaatio-service/build/libs/organisaatio-service.jar 
```
Tämän ajamiseen pitää olla dev.properties classpathissa, jossa olisi hyvä olla ainakin seuraavat avain-arvo parit (en ole ihan varma mitkä on välttämättömiä):
```
organisaatio.service.username=xxx
organisaatio.service.password=xxx
organisaatio.service.username.to.koodisto=
organisaatio.service.password.to.koodisto=
organisaatio-service.scheduled.update.cron.expression= 0 * * * * ?
organisaatio.service.username.to.viestinta=
organisaatio.service.password.to.viestinta=
organisaatio-service.postgresql.url=
organisaatio-service.postgresql.user=
organisaatio-service.postgresql.password=
host.ilb=
host.alb=
spring.datasource.url=jdbc:postgresql://localhost:5432/organisaatio
spring.datasource.username=app
spring.datasource.password=ophoph
cas.service=
root.organisaatio.oid=1.2.246.562.10.00000000001
ryhmasahkoposti.service.email=
url-virkailija=http://localhost:9000
host.virkailija=localhost:9000
```

#### Frontend
asenna ensin riippuvuudet ajamalla organisaatio-ui hakemistossa ensin
``` bash
npm install
```
käynnistä sitten ui ajamalla:
``` bash
npm run start
```
ui käynnistyy porttiin 3000. Avaa http://localhost:3000/organisaatio selaimessa. Tarkasta verkkokutsuista failanneet pyynnöt ja kopioi joku niistä osoiteriville niin pääset autentikoitumaan.
Kun tämä on tehty, organisaatiopalvelu aukeaa localhostissa oikein.
### Profiilit
dev-profiili toimii ilman käyttöoikeuksia, ja antaa käyttäjälle cas-kirjautumisen jälkeen suoraan täydet oikat. KTS. fi.vm.sade.organisaatio.config.DevUserDetailsServiceConfiguration.

## IDEA Kehitys
Git repoon on tallennettu .run hakemistoon valmiita ajokonfigurointeja backend+mockapi ja frontend+javascript-debug ajoja varten.
       
## Opetushallituksen palvelukokonaisuus
https://confluence.csc.fi/display/OPHPALV/Opetushallituksen+palvelukokonaisuus
