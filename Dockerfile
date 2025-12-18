FROM maven:3.9.12-amazoncorretto-21-al2023@sha256:cfce7b85a5873d8cddd84c234dc55d898c4d32048b440da3eab309bb67ee0b81 AS build
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
COPY settings.xml .
COPY pom.xml .

RUN mvn clean package -s settings.xml -DskipTests

FROM amazoncorretto:21.0.9@sha256:c26e10ed8cf8300aacc0952a4869ed64e14d47d2cfdb24235b891233e4c5385c
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
