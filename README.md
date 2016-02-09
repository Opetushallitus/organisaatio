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

https://testi.virkailija.opintopolku.fi/organisaatio-service/swagger/index.html


## Sovelluksen ajaminen omalla koneella

### OPH:n kehityspalvelinta vasten

Prosessi:

    Kloonaa repository Githubista
    OPH:n Maven-repoihin viittaava settings.xml paikoilleen oman kotihakemiston alta löytyvään .m2-hakemistoon
    Konfiguraatiotiedostot paikoilleen oman kotihakemiston alle luotavaan "oph-configuration" -hakemistoon
    Käännä palvelu organisaatio-repositoryn juurihakemistossa
        mvn clean install

    Muokkaa repositoryn konffitiedostoa servers/src/main/webapp/META-INF/jetty-env.xml ja muuta localhostin tilalle kehityspalvelimen osoite niin saat yhteyden luokka-ympäristön kantaan
    Aja palvelua lokaalisti:
        cd servers
        mvn jetty:run
    (vaihtoehtoisesti IDEAn jetty-pluginilla)


Autentikointi/auktorisointi:

    Konffit on nyt viritetty niin, että sovellus ajetaan lokaalisti, mutta tietokanta on integraatiotestiympäristön kanta, joka sijaitsee OPH:n tietokantapalvelimella. Samoin viittaukset muihin palveluihin, joista organisaatiopalvelu on riippuvainen on konffattu osoittamaan testiympäristöön.
    Lokaali UI testiympäristö Jetty-palvelimen ollessa päällä
        http://localhost:8180/organisaatio-ui/

        Autentikaatiossa ja auktorisoinnissa käytetään kehitysmoodia, jolloin käyttäjätiedot ja roolit löytyvät oph-configuration-hakemistossa olevasta security-context-backend.xml-tiedostosta. Eli kun avaa lokaalisti käyttöliittymän, avautuvaan basic auth -loginiin kirjoitettavat tunnukset ja salasanat löytyvät tuolta.

        Lokaalin backendin tarjoamat REST-rajapinnat ja niiden dokumentaatio
            http://localhost:8180/organisaatio-service
            http://localhost:8180/organisaatio-service/swagger/index.html
