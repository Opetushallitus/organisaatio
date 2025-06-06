#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  echo "$repo"
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  source "$repo/scripts/lib/nvm.sh"

  nvm use 20 || nvm install -b 20 && nvm use 20

  cd "$repo/mock-api"
  npm ci
  npm run mock-api
}

main "$@"
