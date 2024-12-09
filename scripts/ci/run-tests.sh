#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

trap stop_database EXIT

function main {
  init_nodejs
  install_npm_dependencies
  start_database
  build_and_test_frontend
  build_and_test_jar
  run_cypress_tests
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

function build_and_test_frontend {
  cd $repo/organisaatio-ui
  npm run lint
  npm run prettier
  CI=true npm run test
  npm run build
}

function build_and_test_jar {
  cd $repo
  ./gradlew clean build
}

function run_cypress_tests {
  cd "${repo}/mock-api" && npm run mock-api &
  mock_api_pid=$!
  cd "${repo}/organisaatio-ui" && npm run start &
  ui_pid=$!
  cd "${repo}"
  java -jar -Xms2g -Xmx2g \
    -Dspring.config.location=classpath:application.properties,classpath:application-test-envs.properties \
    -Dspring.profiles.active=dev \
    -Dspring.flyway.enabled=true \
    -Durl-virkailija=http://localhost:9000 \
    -Dhost.virkailija=localhost:9000 \
    -Durl-ytj=http://localhost:9000/ytj \
    -Durl-oidservice=http://localhost:9000/oidservice \
    -Dcas.service.organisaatio-service=http://localhost:8080/organisaatio-service-not-available \
    organisaatio-service/build/libs/organisaatio-service.jar &

  cd "${repo}/organisaatio-ui"
  npm run cypress:ci

  kill $mock_api_pid
  kill $ui_pid
}

main "$@"