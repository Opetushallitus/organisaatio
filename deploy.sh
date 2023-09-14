#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  local -r ENV="$1"
  local -r VERSION="$2"
  local -r SERVICE="organisaatio"

  init_cloud_base_virtualenv

  info "checking changes to cloud-environment repo"
  pushd "$repo/../cloud-environment-$ENV"
  git pull --rebase
  popd

  pushd "$repo/../cloud-base"
  copy_mfa_token_to_clipboard
  ./aws/config.py "$ENV" publish

  info "Checking currently running version"
  info "./aws/deploy.py \"$ENV\" \"$SERVICE\" image"
  ./aws/deploy.py "$ENV" "$SERVICE" image

  info "./aws/cloudformation.py \"$ENV\" services update -s \"$SERVICE\""
  ./aws/cloudformation.py "$ENV" services update -s "$SERVICE"

  info "Deploying version $VERSION to $ENV"
  info "./aws/deploy.py \"$ENV\" \"$SERVICE\" deploy --version \"$VERSION\" --force"
  ./aws/deploy.py "$ENV" "$SERVICE" deploy --version "$VERSION" --force
  popd
}

function init_cloud_base_virtualenv {
  pushd "$repo/../cloud-base"
  git pull --rebase
  . oph-venv/bin/activate
  pip install -r requirements.txt
  popd
}

function copy_mfa_token_to_clipboard {
  info "Copying MFA token to clipboard..."
  op item get "AWS OPH" --otp | pbcopy
  info "Copied!"
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

  >&2 echo -e "${timestamp} ${level} ${message}"
}

main "$@"
