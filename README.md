# Organisaatioiden hallintapalvelu

Sisältää sovellukset

    Organisaatiopalvelun REST- ja SOAP-palvelut
        organisaatio-service
    Käyttöliittymä
        organisaatio-ui

Ja artifactoryyn asennettavat kirjastot

    organisaatio-api (wsdl-kuvaukset ja REST-rajapintakuvaukset)
    organisaatio-domain (domain-luokat REST-rajapintojen käyttöön)


## Swagger-dokumentaatio

https://virkailija.opintopolku.fi/organisaatio-service/swagger/index.html


## Sovelluksen pystyttäminen ja ajaminen omalla koneella
Ohje: https://confluence.csc.fi/pages/viewpage.action?pageId=61410939

## Opetushallituksen palvelukokonaisuus
https://confluence.csc.fi/display/OPHPALV/Opetushallituksen+palvelukokonaisuus


Täysin uusi versio Tärkeää:
Flyway pitää ensin käyttää versiossa 4.2.0 ennen versioon 5 siirtymistä, jotta checksum virheet korjaantuu migraatioissa.
Checksumit voi korjata käsin tietokantatauluun myös.