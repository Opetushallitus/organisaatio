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

main "$@"