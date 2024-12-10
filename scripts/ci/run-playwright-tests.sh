#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

trap stop_database EXIT

function main {
  init_nodejs
  install_npm_dependencies

  start_database
  start_mock_api
  start_ui
  start_server

  cd "${repo}/playwright"
  npx playwright install --with-deps
  npx playwright test
}

function start_mock_api {
  cd "${repo}/mock-api" && npm run mock-api &
  wait_for_port 9000
}

function start_ui {
  cd "${repo}/organisaatio-ui" && npm run start &
  wait_for_port 3003
}

function start_server {
  cd "${repo}"
  ./gradlew clean build -x test
  java -jar \
    -Dspring.config.location=classpath:application.properties,classpath:application-test-envs.properties \
    -Dspring.profiles.active=dev \
    -Dspring.flyway.enabled=true \
    -Durl-virkailija=http://localhost:9000 \
    -Dhost.virkailija=localhost:9000 \
    -Durl-ytj=http://localhost:9000/ytj \
    -Durl-oidservice=http://localhost:9000/oidservice \
    -Dcas.service.organisaatio-service=http://localhost:8080/organisaatio-service-not-available \
    organisaatio-service/build/libs/organisaatio-service.jar &
    wait_for_port 8080
}

function install_npm_dependencies {
  for bom_dir in "${repo}/playwright" \
                 "${repo}/organisaatio-ui" \
                 "${repo}/mock-api"; do
    cd ${bom_dir}
    npm_ci_if_needed
  done
}

function start_database {
  cd $repo
  docker compose up --detach
}

function stop_database {
  cd $repo
  docker compose down
}

function wait_for_port {
  local -r port="$1"
  while ! nc -z localhost $port; do
    sleep 1; echo "Waiting for port $port to respond"
  done
}

main "$@"
