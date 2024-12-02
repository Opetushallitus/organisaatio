#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/scripts/lib/common-functions.sh"

function main {
  local -r ENV="$1"; shift
  AWS_PROFILE="oph-organisaatio-${ENV}" aws "$@"
}

main "$@"
