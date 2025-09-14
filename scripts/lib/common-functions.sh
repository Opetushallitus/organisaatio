export repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"
readonly node_version=$( cat "$repo/.nvmrc" )

function wait_for_container_to_be_healthy {
  require_docker
  local -r container_name="$1"

  info "Waiting for docker container $container_name to be healthy"
  until [ "$(docker inspect -f {{.State.Health.Status}} "$container_name" 2>/dev/null || echo "not-running")" == "healthy" ]; do
    sleep 2
  done
}

function require_docker {
  require_command docker
  docker ps >/dev/null 2>&1 || fatal "Running 'docker ps' failed. Is docker daemon running? Aborting."
}

function require_command {
  if ! command -v "$1" >/dev/null; then
    fatal "I require $1 but it's not installed. Aborting."
  fi
}

function info {
  log "INFO" "$1"
}

function fatal {
  log "ERROR" "$1"
  exit 1
}

function log {
  local -r level="$1"
  local -r message="$2"
  local -r timestamp=$(date +"%Y-%m-%d %H:%M:%S")

  echo >&2 -e "${timestamp} ${level} ${message}"
}

function parse_env_from_script_name {
  local -r file_name="$(basename "$0")"
  if echo "${file_name}" | grep -E -q '.+-([^-]+)\.sh$'; then
    local -r env="$(echo "${file_name}" | sed -E -e 's|.+-([^-]+)\.sh$|\1|g')"
    info "Using env $env"
    echo $env
  else
    fatal "Don't call this script directly"
  fi
}

function export_aws_credentials {
  local -r env=$1
  export AWS_PROFILE="oph-organisaatio-${env}"

  if ! aws sts get-caller-identity >/dev/null; then
    fatal "AWS credentials are not configured env $env. Aborting."
  fi
}

function aws {
  docker run \
    --platform linux/amd64 \
    --env AWS_PROFILE \
    --env AWS_DEFAULT_REGION \
    --env AWS_CONTAINER_CREDENTIALS_RELATIVE_URI \
    --volume "${HOME}/.aws:/root/.aws" \
    --volume "$( pwd ):/aws" \
    --rm \
    --interactive \
    amazon/aws-cli:2.15.21 "$@"
}

function init_nodejs {
  export NVM_DIR="${NVM_DIR:-$HOME/.cache/nvm}"
  set +o errexit
  source "$repo/scripts/lib/nvm.sh"
  nvm use "${node_version}" || nvm install "${node_version}"
  set -o errexit
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

function is_running_on_codebuild {
  [ -n "${CODEBUILD_BUILD_ID:-}" ]
}
