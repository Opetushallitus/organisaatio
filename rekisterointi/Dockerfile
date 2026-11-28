FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:2d2f6cfef10fa7b8ef897aa98d61ee1d06ac888bba4daa4740e959ff3e3de351 AS build

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

ENV NVM_DIR=/opt/nvm

WORKDIR /app


COPY .nvmrc ./
COPY scripts/lib/nvm.sh ${NVM_DIR}/nvm.sh

RUN source "$NVM_DIR/nvm.sh" --no-use \
  && nvm install

COPY rekisterointi/ ./

WORKDIR /app/rekisterointi-ui
RUN source "$NVM_DIR/nvm.sh" --no-use \
    && nvm use \
    && npm ci \
    && npm run build

WORKDIR /app
RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21@sha256:fe9b50d12d33a499fb382d47370017a8dee73a2ab80ea6d5aa9a30ee72c82caa
WORKDIR /app

COPY --from=build /app/target/rekisterointi.jar rekisterointi.jar
COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

exec java \
  -Dspring.config.additional-location=classpath:/config/rekisterointi.yml,classpath:/config/$ENV.yml \
  -jar rekisterointi.jar
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
