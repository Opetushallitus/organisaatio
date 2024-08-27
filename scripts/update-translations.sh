#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  curl -o "$repo/mock-api/src/api/lokalisointi/cxf/rest/v1/localisation/GET.json" \
    https://virkailija.testiopintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=organisaatio2
}

main "$@"
