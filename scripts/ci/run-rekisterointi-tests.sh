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
  start_server

  cd "${repo}/rekisterointi/rekisterointi-ui"
  npx playwright install --with-deps
  HEADLESS=true npm run playwright:test
}

function start_mock_api {
  cd "${repo}/rekisterointi/mock-api" && npm start &
  wait_for_port 9000
}

function start_server {
  cd "${repo}/rekisterointi/rekisterointi-ui"
  npm run build
  cd "${repo}/rekisterointi"
  mvn clean package -DskipTests -s ../settings.xml
  java -jar -Dspring.profiles.active=dev target/rekisterointi.jar &
  wait_for_port 3000
}

function install_npm_dependencies {
  for bom_dir in "${repo}/rekisterointi/rekisterointi-ui" \
                 "${repo}/rekisterointi/mock-api"; do
    cd ${bom_dir}
    npm_ci_if_needed
  done
}

function start_database {
  cd "${repo}/rekisterointi"
  docker compose up --detach
}

function cleanup {
  cd "${repo}/rekisterointi"
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
