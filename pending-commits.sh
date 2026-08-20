#!/usr/bin/env bash

set -o errexit -o nounset -o pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

source "scripts/lib/common-functions.sh"

readonly REPO_TAG="Opetushallitus/organisaatio"

function main {
  require_command jq

  git fetch --tags --force > /dev/null

  print_pending_commits "$@" | less --no-init --quit-if-one-screen --RAW-CONTROL-CHARS
}

function log_cmd {
  local fmt="%C(bold blue)%h%C(reset) %C(green)(%cI)%C(reset) %s %C(cyan)<%an>%C(reset)"
  git --no-pager log --pretty=format:"$fmt" --color --left-only "$@"
}

function fetch_pipeline_pairs {
  aws resourcegroupstaggingapi get-resources \
    --tag-filters "Key=Repository,Values=${REPO_TAG}" \
    --resource-type-filters codepipeline \
    | jq -c '
        .ResourceTagMappingList
        | map({
            from: ((.Tags // []) | map(select(.Key=="FromBranch"))[0].Value // null),
            to:   ((.Tags // []) | map(select(.Key=="ToBranch"))[0].Value // null)
          })
        | map(select(.from != null and .to != null))
      '
}

function assert_unique_to_branches {
  local -r pairs="$1"
  local duplicates
  duplicates=$(echo "$pairs" | jq -r 'group_by(.to) | map(select(length > 1) | .[0].to) | .[]')
  if [[ -n "${duplicates}" ]]; then
    fatal "Multiple pipelines share the same ToBranch tag: ${duplicates}"
  fi
}

function default_branch {
  local head
  head=$(git symbolic-ref --short refs/remotes/origin/HEAD)
  echo "${head#origin/}"
}

function print_pending_commits {
  export_aws_credentials "util"

  local pairs
  pairs=$(fetch_pipeline_pairs)
  if [[ "$(echo "$pairs" | jq 'length')" -eq 0 ]]; then
    fatal "No CodePipeline pipelines tagged Repository=${REPO_TAG} found in util account."
  fi

  assert_unique_to_branches "$pairs"

  local heads
  heads=$(echo "$pairs" | jq -r '
    . as $p
    | ($p | map(.from)) as $froms
    | $p | map(select(.to as $t | ($froms | index($t)) | not)) | .[].to
  ')
  if [[ -z "${heads}" ]]; then
    fatal "Could not determine the leaf(s) of the deploy chain from pipeline tags."
  fi

  local current from
  while IFS= read -r current; do
    while :; do
      from=$(echo "$pairs" | jq -r --arg t "$current" '.[] | select(.to == $t) | .from')
      if [[ -z "${from}" || "${from}" == "null" ]]; then
        break
      fi
      echo "# Commits for ${from} -> ${current}"
      log_cmd "origin/${from}...origin/${current}" && echo
      current="$from"
    done
  done <<< "${heads}"

  local branch
  branch=$(default_branch)
  echo "# Commits for ${branch} -> origin/${branch}"
  log_cmd "${branch}...origin/${branch}" && echo
}

main "$@"
