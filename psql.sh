#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function main {
  export ENV="$1"
  shift
  local -r SERVICE="organisaatio"

  init_cloud_base_virtualenv
  copy_mfa_token_to_clipboard
  db_username="app"
  db_password="$( get_parameter "/${ENV}/postgresqls/${SERVICE}/app-user-password" )"

  if [ "${ENV}" = "hahtuva" ]; then
    tunnel_port="6691"
    db_hostname="${SERVICE}.db.hahtuvaopintopolku.fi"
  elif [ "${ENV}" = "untuva" ]; then
    tunnel_port="6692"
    db_hostname="${SERVICE}.db.untuvaopintopolku.fi"
  elif [ "${ENV}" = "pallero" ]; then
    tunnel_port="6693"
    db_hostname="${SERVICE}.db.testiopintopolku.fi"
  elif [ "${ENV}" = "sade" ]; then
    tunnel_port="6694"
    db_hostname="${SERVICE}.db.opintopolku.fi"
  fi

  start_tunnel "${tunnel_port}:${db_hostname}:5432"
  PGPASSWORD="${db_password}" psql --host localhost --port "${tunnel_port}" --username "${db_username}" --dbname "${SERVICE}" "$@"
}

function get_parameter {
  local -r parameter_name="$1"
  aws ssm get-parameter \
    --name "${parameter_name}" \
    --with-decryption \
    --region eu-west-1 \
    --profile "oph-${ENV}" \
    --query "Parameter.Value" \
    --output text
}

function start_tunnel {
  local -r tunnel="$1"
  info "Starting SSH tunnel"
  # SSH keeps the connection and tunnel open until both the command executed is finished and all connections through
  # the tunnel are closed. Therefore as long as we have the psql connection open, the tunnel will stay open and close
  # automatically when all connections are closed.
  ssh -f -L "${tunnel}" "${ENV}-bastion" sleep 10
}

function init_cloud_base_virtualenv {
  pushd "$repo/../cloud-base"
  info "Pulling latest cloud-base"
  git pull --rebase
  . oph-venv/bin/activate
  pip install --requirement requirements.txt > /dev/null
  popd
}

function copy_mfa_token_to_clipboard {
  info "Copying MFA token to clipboard"
  op item get "AWS OPH" --otp | pbcopy
}

function info {
  log "INFO" "$1"
}

function log {
  local -r level="$1"
  local -r message="$2"
  local -r timestamp=$(date +"%Y-%m-%d %H:%M:%S")

  >&2 echo -e "${timestamp} ${level} ${message}"
}

main "$@"
