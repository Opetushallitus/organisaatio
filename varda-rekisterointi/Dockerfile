FROM maven:3.9.8-amazoncorretto-21-al2023 AS build
WORKDIR /app

RUN dnf install -y nodejs20 \
  && alternatives --install /usr/bin/node node /usr/bin/node-20 90 \
  && alternatives --install /usr/bin/npm npm /usr/bin/npm-20 90 \
  && alternatives --install /usr/bin/npx npx /usr/bin/npx-20 90

COPY package.json .
COPY package-lock.json .
RUN npm ci

COPY . .
RUN npm run build
RUN mvn clean package -s settings.xml -DskipTests

FROM amazoncorretto:21
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
