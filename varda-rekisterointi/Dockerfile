FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:be93cfd9103cf111289f4725d1fb08dfd6f31d77b77da338f87aa470642e8ce5 AS build

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

ENV NVM_DIR=/opt/nvm

WORKDIR /app


COPY .nvmrc ./
COPY scripts/lib/nvm.sh ${NVM_DIR}/nvm.sh

RUN source "$NVM_DIR/nvm.sh" --no-use \
  && nvm install

COPY varda-rekisterointi/package.json .
COPY varda-rekisterointi/package-lock.json .
RUN source "$NVM_DIR/nvm.sh" --no-use \
    && nvm use \
    && npm ci

COPY varda-rekisterointi/ ./
RUN source "$NVM_DIR/nvm.sh" --no-use \
    && nvm use \
    && npm run build
RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21@sha256:90100f194336ef2326f5efe1ee747395fd87d8210ad6d60463196aa0c4b0ad4e
WORKDIR /app

COPY --from=build /app/target/varda-rekisterointi.jar varda-rekisterointi.jar
COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

exec java \
  -Dspring.config.additional-location=classpath:/config/aws.yml,classpath:/config/$ENV.yml \
  -jar varda-rekisterointi.jar
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
