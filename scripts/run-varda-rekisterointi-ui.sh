#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  echo "${repo}"
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  source "${repo}/scripts/lib/nvm.sh"

  nvm use 20 || nvm install -b 20 && nvm use 20

  cd "${repo}/varda-rekisterointi"
  npm_ci_if_needed
  npm start
}

main "$@"