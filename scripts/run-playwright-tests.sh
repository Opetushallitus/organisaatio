#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  source "$repo/scripts/lib/nvm.sh"

  nvm install -b 16.17.0
  nvm use 16.17.0

  cd "$repo/playwright"
  npm ci
  npx playwright test
}

main "$@"