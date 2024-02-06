# varda-rekisterointi

## Purpose

[explained here](https://wiki.eduuni.fi/pages/viewpage.action?pageId=222569474#Vardaohjeetyksityisilleperhep%C3%A4iv%C3%A4hoitajillejaryhm%C3%A4perhep%C3%A4iv%C3%A4hoitajille-Rekister%C3%B6ityminenVardaanjak%C3%A4ytt%C3%B6oikeuksienuusiminen)

## Technologies & Frameworks

Below is non-exhaustive list of the key technologies & frameworks used in the project.

### Backend

* Spring boot
* Spring security (CAS)
* Spring Data JDBC
* Postgresql
* Flyway
* Lombok
* Swagger
* db-scheduler

### Frontend

* Typescript
* React (CRA)
* Axios
* Material UI
* Styled components

## Requirements

Required tools for building the project:

* Java 21 ([SDKMan!](https://sdkman.io/) or similar recommended to manage JDKs)
* Docker

Project includes maven wrapper so it doesn't have to be installed. Just use `./mvnw` (unix) and `mvnw.cmd` (win).

## Database

Create database by running:

    docker run --name varda-rekisterointi-db -p 5432:5432 -e POSTGRES_USER=varda-rekisterointi -e POSTGRES_PASSWORD=varda-rekisterointi -e POSTGRES_DB=varda-rekisterointi -d postgres:10.9

This will start the container. Later, it can be started and stopped with `docker <start|stop> varda-rekisterointi-db`. Integration tests automatically start and stop a database server.

## Build

    mvn package

## Run

    java -jar target/varda-rekisterointi.jar

Service is available at <http://localhost:8081/varda-rekisterointi>.

### For development

Create dev.yml file in `src/main/resources`, based on the sample file:

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
    
Alternatively, run `mvn -P dev exec:java` to utilize configuration in POM. This requires having the config file `src/main/resources/dev.yml`.

Start frontend

    npm start

Service is available at <https://localhost:8080/varda-rekisterointi>.
Login with <https://localhost:8080/varda-rekisterointi/hakija/login?hetu=010530-998L> (or with any other valid hetu).

To develop backend only, start it with following parameters

    -Dspring.config.additional-location=<path_to_varda_rekisterointi_git>/dev-configuration/dev.yml,<path_to_file_created_above>/dev.yml
    -DbaseUrl=https://<test_environment_host>
    -Dvarda-rekisterointi.baseUrl=https://localhost:8080
    -Dserver.port=8080
