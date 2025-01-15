#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )/lib/common-functions.sh"

function force_push_tag {
  local -r tag="$1"
  git tag --force "$tag"
  git push --force origin "refs/tags/$tag:refs/tags/$tag"
}

function main {
  local -r env=$(parse_env_from_script_name)
  force_push_branch "green-${env}"
  force_push_tag "green-${env}-$(date +%s)"
}

function force_push_branch {
  local -r branch="$1"
  git branch --force "${branch}"
  git push --force origin "${branch}:${branch}"
}

main "$@"
