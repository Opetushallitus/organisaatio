FROM maven:3.9.15-amazoncorretto-21-al2023@sha256:4f300190d12bb702860a02b16e68c5f92797f91f05e959e124c2e86d22ae1b51 AS build
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

FROM amazoncorretto:21.0.11@sha256:2e49e9dd1935ea14ec8746e839987f136fb38cdb89ad66e5ab09d7f2019e0d53
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
