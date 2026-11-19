FROM maven:3.10.0-amazoncorretto-21-al2023@sha256:c287cc32273f94ac4ab07ba68c92809374db696633307ae06887d902f4bfb93e AS build

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

FROM amazoncorretto:25@sha256:288b5647146a21027688b52cc23d802ee6badb6f28632111b606b79262d9fc5f
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
