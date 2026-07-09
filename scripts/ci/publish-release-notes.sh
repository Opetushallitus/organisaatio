#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../lib/common-functions.sh"

function main {
  local -r env=$(parse_env_from_script_name)

  cd "$repo/scripts/ci"
  init_nodejs
  npm_ci_if_needed
  ENVIRONMENT_NAME=${env} node publish-release-notes.ts
}

main "$@"
