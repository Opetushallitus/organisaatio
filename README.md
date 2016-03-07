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


## Sovelluksen ajaminen omalla koneella
Tarvittava asennettuna: Git-client (esim. SourceTree), Maven 3, JDK 1.8, IntelliJ Idea (tai Eclipse).

Windows: Jetty:ä ajettaessa windows lukitsee oletuksena ajettavat tiedostot. Tämän voi kiertää Jettyn 9 versiossa editoimalla webdefault.xml -tiedostoa lokaalissa maven repositoryssa. Tiedosto löytyy esim. osoitteesta "C:\Users\<username>\.m2\repository\org\eclipse\jetty\jetty-webapp\9.2.1.v20140609\jetty-webapp-9.2.1.v20140609.jar". Etsi seuraava kohta ja muokkaa se vastaavanlaiseksi

    <init-param>
        <param-name>useFileMappedBuffer</param-name>
        <param-value>false</param-value>
    </init-param>

### OPH:n kehityspalvelinta vasten

Asennus:

Kloonaa repository Githubista

    git clone https://github.com/Opetushallitus/organisaatio.git
(optional) OPH:n Maven-repoihin viittaava settings.xml paikoilleen oman kotihakemiston alta löytyvään .m2-hakemistoon (nämä tulevat projektiin automaattisesti build-parent riippuvuuden kautta)
Konfiguraatiotiedostot (common.properties, organisaatio-ui.properties) paikoilleen oman kotihakemiston alle luotavaan "oph-configuration"-hakemistoon.
 
    windows: %homepath%
    linux: ~
Jos näitä tiedostoja ei ole annettu, organisaatio-ui.properties löytyy projektista kansion organisaatio-ui/resources alta. Tähän pitää muuttaa "localhost:portti" => <integraatiopalvelimen osoite>. Common.properties löytyy luokalta kansiosta "/data00/oph/organisaatio/oph-configuration/" ja sen voi kopioida sellaisenaan.
Käännä palvelu organisaatio-repositoryn juurihakemistossa

    mvn clean install

Ajaminen:

Varmista, että repositoryn konffitiedostossa servers/src/main/webapp/META-INF/jetty-env.xml ei ole localhostin vaan kehityspalvelimen osoite niin saat yhteyden luokka-ympäristön kantaan.
Aja palvelua lokaalisti:

    cd servers
    mvn jetty:run
(vaihtoehtoisesti IDEAn jetty-pluginilla)
    
Yllä olevat asennus ja ajamis-komennot on hyvä konfiguroida IDEA kehitysympäristöön kehityksen nopeuttamiseksi.

Autentikointi/auktorisointi:

Konffit on nyt viritetty niin, että sovellus ajetaan lokaalisti, mutta tietokanta on integraatiotestiympäristön kanta, joka sijaitsee OPH:n tietokantapalvelimella. Samoin viittaukset muihin palveluihin, joista organisaatiopalvelu on riippuvainen on konffattu osoittamaan integraatioympäristöön.
Lokaali UI testiympäristö Jetty-palvelimen ollessa päällä
    `http://localhost:8180/organisaatio-ui/`

Autentikaatiossa ja auktorisoinnissa käytetään kehitysmoodia, jolloin käyttäjätiedot ja roolit löytyvät oph-configuration-hakemistossa olevasta security-context-backend.xml-tiedostosta. Eli kun avaa lokaalisti käyttöliittymän, avautuvaan basic auth -loginiin kirjoitettavat tunnukset ja salasanat löytyvät tuolta.
Jos tätä tiedostoa ei ole annettu, sen voi hakea luokalta kansiosta "/data00/oph/organisaatio/oph-configuration". "authentication-manager" tagien sisään laitetaan laitetaan käyttäjä, jolla on vähintään seuraavat oikeudet: (muista asettaa tunnus ja salasana)

    <authentication-provider><user-service>
     <user name="<tunnus>" password="<salasana>" authorities=
     ROLE_APP_ORGANISAATIOHALLINTA, ROLE_APP_ORGANISAATIOHALLINTA_CRUD, 
     ROLE_APP_ORGANISAATIOHALLINTA_CRUD_1.2.246.562.10.00000000001, ROLE_APP_ORGANISAATIOHALLINTA_READ, 
     ROLE_APP_ORGANISAATIOHALLINTA_READ_1.2.246.562.10.00000000001, ROLE_APP_ORGANISAATIOHALLINTA_READ_UPDATE, 
     ROLE_APP_ORGANISAATIOHALLINTA_READ_UPDATE_1.2.246.562.10.00000000001" />
     </user-service></authentication-provider>

Lokaalin backendin tarjoamat REST-rajapinnat ja niiden dokumentaatio:

    http://localhost:8180/organisaatio-service/rest/<rajapinta>
    http://localhost:8180/organisaatio-service/swagger/index.html

SOAP rajapinnat:

    http://localhost:8180/organisaatio-service/

UI:

    http://localhost:8180/organisaatio-ui/
