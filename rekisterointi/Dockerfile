FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:c287cc32273f94ac4ab07ba68c92809374db696633307ae06887d902f4bfb93e AS build

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

FROM amazoncorretto:25@sha256:d4b9047349107b5c5a3c67f89853c48a83d4c769d4196a03f675a4c365737f7b
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
