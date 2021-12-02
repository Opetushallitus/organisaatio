# Organisaatioiden hallintapalvelu

Sisältää sovellukset

    Organisaatiopalvelun REST- ja SOAP-palvelut
        organisaatio-service
    Käyttöliittymä
        organisaatio-ui

Ja artifactoryyn asennettavat kirjastot

    organisaatio-api (wsdl-kuvaukset ja REST-rajapintakuvaukset)
    organisaatio-domain (domain-luokat REST-rajapintojen käyttöön)

## Technologies & Frameworks

Below is non-exhaustive list of the key technologies & frameworks used in the project.

### Backend

* Spring Framework
* Spring Security (CAS)
* Postgresql
* QueryDSL
* JPA / Hibernate5
* Flyway
* Swagger

### Frontend

* Node 8
* Webpack 1.12
* Angular 1.5
* JQuery 2.1
* Bootstrap

### Build tools

* Java 11
* Maven 3
* Docker

## Swagger-dokumentaatio

https://virkailija.opintopolku.fi/organisaatio-service/swagger/index.html


## Sovelluksen pystyttäminen ja ajaminen omalla koneella
Ohje: https://confluence.csc.fi/pages/viewpage.action?pageId=61410939

### UI: kehittäminen omalla koneella

Onnistuu helpoiten käyttämällä [paikallista välityspalvelinta](nginx)

## Opetushallituksen palvelukokonaisuus
https://confluence.csc.fi/display/OPHPALV/Opetushallituksen+palvelukokonaisuus
