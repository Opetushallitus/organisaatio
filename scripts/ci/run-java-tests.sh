#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

trap stop_database EXIT

function main {
  start_database

  cd "${repo}"

  if is_running_on_codebuild; then
    "${repo}"/mvnw clean install -s ../settings.xml
  else
    "${repo}"/mvnw clean install
  fi
}

function start_database {
  cd $repo
  docker compose up --detach
}

function stop_database {
  cd $repo
  docker compose down
}

main "$@"
