#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"
source "${repo}/scripts/lib/common-functions.sh"

function main {
  init_nodejs
  install_npm_dependencies

  cd "${repo}/organisaatio-ui"
  npm run lint
  npm run prettier
  CI=true npm run test
  npm run build
}

function install_npm_dependencies {
  for bom_dir in "${repo}/playwright" \
                 "${repo}/organisaatio-ui" \
                 "${repo}/mock-api"; do
    cd ${bom_dir}
    npm_ci_if_needed
  done
}

main "$@"
