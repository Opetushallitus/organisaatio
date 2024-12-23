#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  wait_for_local_db_to_be_healthy
  select_java_version "21"
  "$repo"/gradlew :organisaatio-service:run
}

function select_java_version {
  java_version="$1"
  JAVA_HOME="$(/usr/libexec/java_home -v "${java_version}")"
  export JAVA_HOME
}

function wait_for_local_db_to_be_healthy {
  wait_for_container_to_be_healthy oph-organisaatio-db
}

main "$@"
