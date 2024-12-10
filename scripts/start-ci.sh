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
  local -r pipeline_name="Deploy$(capitalize "${env}")"
  aws codepipeline start-pipeline-execution --name "$pipeline_name"
  open_codepipeline_in_browser "$pipeline_name"
}

function open_codepipeline_in_browser {
  local -r pipeline_name="$1"
  account_id="$( aws sts get-caller-identity --query Account --output text )"
  open "https://oph-aws-sso.awsapps.com/start/#/console?account_id=${account_id}&role_name=AdministratorAccess&destination=https%3A%2F%2Feu-west-1.console.aws.amazon.com%2Fcodesuite%2Fcodepipeline%2Fpipelines%2F${pipeline_name}%2Fview%3Fregion%3D$( aws configure get region )"
}

function capitalize {
  local -r string="$1"
  echo "$(tr '[:lower:]' '[:upper:]' <<<${string:0:1})${string:1}"
}

main "$@"
