FROM gradle:8.11-jdk21-corretto AS build
WORKDIR /app

RUN dnf install -y nodejs20 \
  && alternatives --install /usr/bin/node node /usr/bin/node-20 90 \
  && alternatives --install /usr/bin/npm npm /usr/bin/npm-20 90 \
  && alternatives --install /usr/bin/npx npx /usr/bin/npx-20 90

COPY organisaatio-ui ./organisaatio-ui
WORKDIR /app/organisaatio-ui
RUN npm ci
RUN npm run build

WORKDIR /app
COPY github-packages-gradle.properties /opt/gradle/gradle.properties
COPY ytj-client ./ytj-client
COPY organisaatio-api ./organisaatio-api
COPY organisaatio-service ./organisaatio-service
COPY gradle ./gradle
COPY settings.gradle .
COPY gradle.properties .
COPY build.gradle .

RUN gradle clean build -x test

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/organisaatio-service/build/libs/organisaatio-service.jar organisaatio.jar
COPY --chmod=755 <<"EOF" /app/entrypoint.sh
#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

exec java \
  -Dspring.config.additional-location=classpath:/config/organisaatio.yml,classpath:/config/$CONFIG_FILE.yml \
  -Denv.name=$ENV \
  -Dlogging.config=classpath:/config/logback.xml \
  -Dserver.port=8080 \
  -jar organisaatio.jar
EOF

ENTRYPOINT [ "/app/entrypoint.sh" ]
