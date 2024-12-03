#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/scripts/lib/common-functions.sh"

function main {
  local -r env=$(parse_env_from_script_name)

  case "${env}" in
    "hahtuva" | "dev" | "qa" | "prod" | "util")
      start_ci "${env}"
      ;;
    *)
      fatal "Unknown env $env"
      ;;
  esac
}

function start_ci {
  local -r env=$1
  require_docker
  export_aws_credentials "util"
  echo "Starting ci for $1"
  aws codepipeline start-pipeline-execution --name "Deploy$(capitalize "${env}")"
}

function capitalize {
  local -r string="$1"
  echo "$(tr '[:lower:]' '[:upper:]' <<<${string:0:1})${string:1}"
}

main "$@"
