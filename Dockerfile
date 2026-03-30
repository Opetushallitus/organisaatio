FROM maven:3.9.12-amazoncorretto-21-al2023@sha256:349b48414980571f18c71506fc259d9b37873546d4d3869fe4ee233c3f437017 AS build
WORKDIR /app

RUN dnf install -y nodejs24 \
  && alternatives --install /usr/bin/node node /usr/bin/node-24 90 \
  && alternatives --install /usr/bin/npm npm /usr/bin/npm-24 90 \
  && alternatives --install /usr/bin/npx npx /usr/bin/npx-24 90

COPY organisaatio-ui ./organisaatio-ui
WORKDIR /app/organisaatio-ui
RUN npm ci
RUN npm run build

WORKDIR /app
COPY ytj-client ./ytj-client
COPY organisaatio-service ./organisaatio-service
COPY codebuild-mvn-settings.xml .
COPY pom.xml .

RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.10@sha256:0560910f891ef2d1277539b022de1f6c032d273f0f1e8444f45b7ef8005463c2
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
