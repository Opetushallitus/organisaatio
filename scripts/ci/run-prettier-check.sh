#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

function main {
  init_nodejs

  cd "${repo}/infra"
  npm_ci_if_needed
  npx prettier . --check
}

main "$@"
