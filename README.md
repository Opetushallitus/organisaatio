# Organisaatioiden hallintapalvelu

Sisältää sovellukset

    Organisaatiopalvelun REST- ja SOAP-palvelut
        organisaatio-service
    Käyttöliittymä
        organisaatio-ui
 


## Swagger-dokumentaatio

https://virkailija.opintopolku.fi/organisaatio/swagger-ui.html


## Sovelluksen pystyttäminen ja ajaminen omalla koneella

### Koonti: 

Aja projektin juuressa

``` bash
   .\gradlew clean build
```
### Käynnistäminen

   Käynnistä backend jar:sta tai spring boot configuraatiolla ideasta.
   
   Käynnistys Jar:sta juuressa.
   
    ``` bash
       java -jar -Dspring.config.location=classpath:application.properties,classpath:dev.properties -Dspring.profiles.active=dev ./organisaatio-service/build/libs/organisaatio-service.jar 
    ```
   Tämän ajamiseen pitää olla dev.properties classpathissa, jossa olisi hyvä olla ainakin seuraavat avain-arvo parit (en ole ihan varma mitkä on välttämättömiä):
       ``` yml
       organisaatio.service.username=
       organisaatio.service.password=
       organisaatio.service.username.to.koodisto=
       organisaatio.service.password.to.koodisto=
       organisaatio-service.scheduled.update.cron.expression=
       organisaatio.service.username.to.viestinta=
       organisaatio.service.password.to.viestinta=
       organisaatio-service.postgresql.url=
       organisaatio-service.postgresql.user=
       organisaatio-service.postgresql.password=
       host.virkailija=
       host.ilb=
       host.alb=
       jdbc:postgresql://localhost:5432/organisaatio
       spring.datasource.username=app
       spring.datasource.password=ophoph
       cas.service=
       root.organisaatio.oid=
       ryhmasahkoposti.service.email=
       ```
       
       asenna ensin riippuvuudet ajamalla organisaatio-ui hakemistossa ensin
       ``` bash
            npm install
       ```
       käynnistä sitten ui ajamalla:
      ``` bash
           npm run start
      ```
      ui käynnistyy porttiin 3000. Avaa http://localhost:3000 selaimessa. Tarkasta verkkokutsuista failanneet pyynnöt ja kopioi joku niistä osoiteriville niin pääset autentikoitumaan.
       Kun tämä on tehty, organisaatiopalvelu aukeaa localhostissa oikein.
### Profiilit
    dev-profiili toimii ilman käyttöoikeuksia, ja antaa käyttäjälle cas-kirjautumisen jälkeen suoraan täydet oikat. KTS. fi.vm.sade.organisaatio.config.DevUserDetailsServiceConfiguration.
       
       
## Opetushallituksen palvelukokonaisuus
https://confluence.csc.fi/display/OPHPALV/Opetushallituksen+palvelukokonaisuus
