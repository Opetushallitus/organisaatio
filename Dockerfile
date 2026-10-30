FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:be93cfd9103cf111289f4725d1fb08dfd6f31d77b77da338f87aa470642e8ce5 AS build
WORKDIR /app
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

ENV NVM_DIR=/opt/nvm

COPY .nvmrc ./
COPY scripts/lib/nvm.sh ${NVM_DIR}/nvm.sh

RUN source "$NVM_DIR/nvm.sh" --no-use \
  && nvm install

COPY organisaatio-ui ./organisaatio-ui
WORKDIR /app/organisaatio-ui

RUN source "$NVM_DIR/nvm.sh" --no-use \
  && nvm use \
  && npm ci \
  && npm run build

WORKDIR /app
COPY ytj-client ./ytj-client
COPY organisaatio-service ./organisaatio-service
COPY codebuild-mvn-settings.xml .
COPY pom.xml .

RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21@sha256:1a307926702559b0086a5c4d339eaafa9c992b3e8ba54201ac8ebbde31d22f0b
WORKDIR /app

COPY --from=build /app/organisaatio-service/target/organisaatio-service.jar organisaatio-service.jar
COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

exec java \
  -Dspring.config.additional-location=classpath:/config/organisaatio.yml,classpath:/config/$ENV.yml \
  -Denv.name=$ENV \
  -Dlogging.config=classpath:/config/logback.xml \
  -Dserver.port=8080 \
  -jar organisaatio-service.jar
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
