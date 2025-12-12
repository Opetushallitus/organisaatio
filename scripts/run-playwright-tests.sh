#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"

function main {
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  source "$repo/scripts/lib/nvm.sh"

  cd "$repo"
  nvm use || nvm install -b && nvm use

  cd "$repo/playwright"
  npm ci
  npx playwright install --with-deps
  npx playwright test "$@"
}

main "$@"
