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

## Build

    mvn package

## Run

    ./start-local-env.sh
