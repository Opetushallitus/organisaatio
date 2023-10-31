#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  select_java_version "21"
  "$repo"/gradlew :organisaatio-service:run --exclude-task :organisaatio-ui:jar
}

function select_java_version {
  java_version="$1"
  JAVA_HOME="$( /usr/libexec/java_home -v "${java_version}" )"
  export JAVA_HOME
}

main "$@"
