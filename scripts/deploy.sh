#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
repo="$( cd "$(dirname "$0")" && pwd )"
source "${repo}/scripts/lib/common-functions.sh"
node_version=$( cat "$repo/.nvmrc" )

function main {
  local -r env=$(parse_env_from_script_name)

  case "${env}" in
    "hahtuva" | "dev" | "qa" | "prod" | "util")
      deploy "${env}"
      ;;
    *)
      fatal "Unknown env $env"
      ;;
  esac
}

function deploy {
  local -r env="$1"
  require_docker
  init_nodejs
  cd "$repo/infra"
  npm_ci_if_needed
  if [ "${env}" == "util" ]; then
    deploy_util
  else
    deploy_env "${env}"
  fi
}

function deploy_util {
  bootstrap_cdk
  export_aws_credentials "util"
  npx cdk --app "npx ts-node ${repo}/infra/src/cdk-app-util.ts" deploy --require-approval never --all
}

function deploy_env {
  local -r env="$1"
  if ! is_running_on_codebuild; then
    export_aws_credentials "util"
    local -r accountId=$(get_aws_account_id_of_env "${env}")
    local -r region=$(get_aws_region_of_env "${env}")
    export CDK_DEPLOY_TARGET_ACCOUNT=${accountId}
    export CDK_DEPLOY_TARGET_REGION=${region}
  fi
  login_to_docker_if_possible
  ENV=${env} npx cdk --app "npx ts-node ${repo}/infra/src/cdk-app.ts" deploy --require-approval never --all
}

function login_to_docker_if_possible {
  if [ -n "${DOCKER_USERNAME:-}" ] && [ -n "${DOCKER_PASSWORD:-}" ]; then
    info "Logging in to dockerhub"
    echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
  else
    info "Not logging into dockerhub"
  fi
}

function bootstrap_cdk {
  util_account_id=$( get_aws_account_id_of_env "util" )

  local env
  for env in util hahtuva dev qa prod; do
    info "Bootstrapping $env"
    bootstrap_cdk_for_env "${util_account_id}" "${env}"
  done
}

function bootstrap_cdk_for_env {
  local -r util_account_id=$1
  local -r env=$2
  local -r account_id=$(get_aws_account_id_of_env "${env}")
  local -r region=$(get_aws_region_of_env "${env}")
  local -r policy_name="CDKDeploymentTargetPermissions"

  export_aws_credentials "${env}"
  info "Bootstrapping CDK for env ${env} at ${account_id}/${region}"

  if [ "${env}" == "util" ]; then
      npx cdk bootstrap "aws://${account_id}/${region}"
  else
      setup_cdk_deployment_target_policy "${policy_name}"
      npx cdk bootstrap "aws://${account_id}/${region}" \
          --trust "${util_account_id}" \
          --trust-for-lookup "${util_account_id}" \
          --cloudformation-execution-policies "arn:aws:iam::${account_id}:policy/${policy_name}"
      npx cdk bootstrap "aws://${account_id}/us-east-1" \
          --trust "${util_account_id}" \
          --trust-for-lookup "${util_account_id}" \
          --cloudformation-execution-policies "arn:aws:iam::${account_id}:policy/${policy_name}"

      info "Setting up CDK deployment target policy for env ${env}"
  fi
}

function setup_cdk_deployment_target_policy {
  local -r policyName=$1
  POLICY_NAME=${policyName} npx ts-node "${repo}/infra/src/setup-cdk-deployment-target-policy.ts"
}

function is_running_on_codebuild {
  [ -n "${CODEBUILD_BUILD_ID:-}" ]
}

function export_aws_credentials {
  local -r env=$1
  export AWS_PROFILE="oph-organisaatio-${env}"

  if ! aws sts get-caller-identity >/dev/null; then
    fatal "AWS credentials are not configured env $env. Aborting."
  fi
}

function get_aws_region_of_env {
  local -r env=$1
  get_env_specific_param "${env}" region
}

function get_aws_account_id_of_env {
  local -r env=$1
  get_env_specific_param "${env}" account_id
}

function get_env_specific_param {
  local -r env=$1
  local -r param=$2
  if ! is_running_on_codebuild; then
    export_aws_credentials "util"
  fi
  aws ssm get-parameter --name "/env/${env}/${param}" --query Parameter.Value --output text
}

function get_aws_account_id {
  aws sts get-caller-identity --query Account --output text
}

function npm_ci_if_needed {
  require_command shasum

  if [ ! -f "package.json" ]; then
    fatal "package.json is missing"
  elif [ ! -f "package-lock.json" ]; then
    info "package-lock.json is missing"
    npm install
  elif [ ! -f "$( npm root )/package.json.checksum" ]; then
    info "package.json checksum missing"
    npm ci
  elif [ ! -f "$( npm root )/package-lock.json.checksum" ]; then
    info "package-lock.json checksum missing"
    npm ci
  elif ! shasum --check "$( npm root )/package.json.checksum"; then
    info "package.json changed"
    npm install
  elif ! shasum --check "$( npm root )/package-lock.json.checksum"; then
    info "package-lock.json changed"
    npm ci
  else
    info "No changes in package.json or package-lock.json"
  fi

  shasum package-lock.json > "$( npm root )/package-lock.json.checksum"
  shasum package.json > "$( npm root )/package.json.checksum"
}

function init_nodejs {
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  set +o errexit
  source "$repo/scripts/lib/nvm.sh"
  nvm use "${node_version}" || nvm install "${node_version}"
  set -o errexit
}

main "$@"
