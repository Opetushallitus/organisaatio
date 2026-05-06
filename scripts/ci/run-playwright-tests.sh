#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

trap cleanup EXIT

function main {
  init_nodejs
  install_npm_dependencies

  start_database
  start_mock_api
  start_uis
  start_servers

  cd "${repo}/playwright"
  npx playwright install --with-deps
  npm run "$1"
}

function start_mock_api {
  cd "${repo}/mock-api" && npm run mock-api &
  wait_for_port 9000
}

function start_uis {
  cd "${repo}/organisaatio-ui" && npm run start &
  cd "${repo}/varda-rekisterointi" && npm run start &
  wait_for_port 3003
  wait_for_port 3000
}

function start_servers {
  cd "${repo}"

  if is_running_on_codebuild; then
    "${repo}"/mvnw clean package -DskipTests -s ./codebuild-mvn-settings.xml
  else
    "${repo}"/mvnw clean package -DskipTests
  fi

  java -jar \
    -Dspring.config.location=classpath:application.properties,classpath:application-test-envs.properties \
    -Dspring.profiles.active=dev \
    -Dspring.flyway.enabled=true \
    -Durl-virkailija=http://localhost:9000 \
    -Dhost.virkailija=localhost:9000 \
    -Durl-ytj=http://localhost:9000/ytj \
    -Durl-oidservice=http://localhost:9000/oidservice \
    -Dcas.service.organisaatio-service=http://localhost:8080/organisaatio-service-not-available \
    organisaatio-service/target/organisaatio-service.jar &

  VALTUUDET_API_KEY="dummy"
  VALTUUDET_CLIENT_ID="dummy"
  VALTUUDET_OAUTH_PASSWORD="dummy"
  SERVICE_USERNAME="dummy"
  SERVICE_PASSWORD="dummy"

  cd "${repo}/varda-rekisterointi"
  ../mvnw spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="
      -Dvarda-rekisterointi.service.username=$SERVICE_USERNAME
      -Dvarda-rekisterointi.service.password=$SERVICE_PASSWORD
      -Dvarda-rekisterointi.palvelukayttaja.client-id=dummy
      -Dvarda-rekisterointi.palvelukayttaja.client-secret=dummy
      -Dotuva.jwt.issuer-uri=http://localhost:9000
      -Dvarda-rekisterointi.valtuudet.api-key=$VALTUUDET_API_KEY
      -Dvarda-rekisterointi.valtuudet.client-id=$VALTUUDET_CLIENT_ID
      -Dvarda-rekisterointi.valtuudet.oauth-password=$VALTUUDET_OAUTH_PASSWORD
      -Dvarda-rekisterointi.url-virkailija=https://virkailija.untuvaopintopolku.fi" &

  wait_for_port 8080
  wait_for_port 8081
}

function install_npm_dependencies {
  for bom_dir in "${repo}/playwright" \
                 "${repo}/organisaatio-ui" \
                 "${repo}/varda-rekisterointi" \
                 "${repo}/mock-api"; do
    cd ${bom_dir}
    npm_ci_if_needed
  done
}

function start_database {
  cd $repo
  docker compose up --detach
}

function cleanup {
  cd $repo
  docker compose down || true
  kill $( jobs -p ) || true
}

function wait_for_port {
  local -r port="$1"
  while ! nc -z localhost $port; do
    sleep 1; echo "Waiting for port $port to respond"
  done
}

main "$@"
