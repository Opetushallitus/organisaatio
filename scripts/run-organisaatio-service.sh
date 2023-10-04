#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  "$repo"/gradlew :organisaatio-service:run --exclude-task :organisaatio-ui:jar
}

main "$@"
