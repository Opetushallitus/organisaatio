#!/usr/bin/env bash

set -o errexit -o nounset -o pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

git fetch --tags --force > /dev/null

function log_cmd {
  local fmt="%C(bold blue)%h%C(reset) %C(green)(%cI)%C(reset) %s %C(cyan)<%an>%C(reset)"
  git --no-pager log --pretty=format:"$fmt" --color --left-only "$@"
}

function print_pending_commits {
  echo "# Commits for qa -> prod"
  log_cmd origin/green-qa...origin/green-prod && echo

  echo "# Commits for dev -> qa"
  log_cmd origin/green-dev...origin/green-qa && echo

  echo "# Commits for hahtuva -> dev"
  log_cmd origin/green-hahtuva...origin/green-dev && echo

  echo "# Commits for origin/master-> hahtuva"
  log_cmd origin/master...origin/green-hahtuva && echo

  echo "# Commits for master -> origin/master"
  log_cmd master...origin/master && echo
}

print_pending_commits "$@" | less --no-init --quit-if-one-screen --RAW-CONTROL-CHARS
