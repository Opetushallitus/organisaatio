#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  echo "$repo"
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  source "$repo/scripts/lib/nvm.sh"

  cd "$repo"
  nvm use || nvm install -b && nvm use

  cd "$repo/organisaatio-ui"
  npm ci --force
  npm start
}

main "$@"
