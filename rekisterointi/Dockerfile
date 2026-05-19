FROM maven:3.9.16-amazoncorretto-21-al2023@sha256:6f36958286479d0cf966ec8f776cc7e164cd894bfa9a6e682ba02af203c0a69e AS build
WORKDIR /app

RUN dnf install -y nodejs24 \
  && alternatives --install /usr/bin/node node /usr/bin/node-24 90 \
  && alternatives --install /usr/bin/npm npm /usr/bin/npm-24 90 \
  && alternatives --install /usr/bin/npx npx /usr/bin/npx-24 90

COPY . .

WORKDIR /app/rekisterointi-ui
RUN npm ci
RUN npm run build

WORKDIR /app
RUN mvn clean package -s codebuild-mvn-settings.xml -DskipTests

FROM amazoncorretto:21.0.11@sha256:cc5af6b7df0e0bf2f5952dd53fdc90e011d33c26e31c749357227808dca871d7
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
