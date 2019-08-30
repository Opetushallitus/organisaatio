# varda-rekisterointi

## Requirements

Java 11

Project includes maven wrapper so it doesn't have to be installed. Just use `./mvnw` (unix) and `mvnw.cmd` (win).

## Database

    docker run --name varda-rekisterointi-db -p 5432:5432 -e POSTGRES_USER=varda-rekisterointi -e POSTGRES_PASSWORD=varda-rekisterointi -e POSTGRES_DB=varda-rekisterointi -d postgres:10.9

## Build

    mvn package

## Run

    java -jar target/varda-rekisterointi.jar

Service is available at <http://localhost:8080/varda-rekisterointi>.

### For development

Create dev.yml file outside git with following content:

    varda-rekisterointi:
        service:
            username: <your_username>
            password: <your_password>
        valtuudet:
            host: https://asiointivaltuustarkastus.test.suomi.fi
            client-id: <client_id>
            api-key: <api_key>
            oauth-password: <oauth_password>

Start backend with following parameters

    -Dspring.config.additional-location=<path_to_file_created_above>/dev.yml
    -DbaseUrl=https://<test_environment_host>
    -Dvarda-rekisterointi.baseUrl=https://localhost:8080

Start frontend

    npm start

Service is available at <https://localhost:8080/varda-rekisterointi>.
Login with <https://localhost:8080/varda-rekisterointi/hakija/login?hetu=010530-998L> (or with any other valid hetu).

To develop backend only, start it with following parameters

    -Dspring.config.additional-location=<path_to_varda_rekisterointi_git>/dev-configuration/dev.yml,<path_to_file_created_above>/dev.yml
    -DbaseUrl=https://<test_environment_host>
    -Dvarda-rekisterointi.baseUrl=https://localhost:8080
    -Dserver.port=8080
