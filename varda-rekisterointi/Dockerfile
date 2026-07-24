FROM maven:3.9.16-amazoncorretto-21-al2023@sha256:d2dddb696d1c1bd8c0cfb4fe6eb64c1ef4c91197d5eda1f6008db2f8d4aeb811 AS build
WORKDIR /app

RUN dnf install -y nodejs24 \
  && alternatives --install /usr/bin/node node /usr/bin/node-24 90 \
  && alternatives --install /usr/bin/npm npm /usr/bin/npm-24 90 \
  && alternatives --install /usr/bin/npx npx /usr/bin/npx-24 90

COPY package.json .
COPY package-lock.json .
RUN npm ci

COPY . .
RUN npm run build
RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:25.0.3@sha256:d1ac78aed1aa34badfc02bd732f869636eb2d610fbf01cb3bd79eacd458c48a4
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
