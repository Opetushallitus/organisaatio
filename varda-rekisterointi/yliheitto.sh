#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/../scripts/lib/common-functions.sh"

export OPINTOPOLKU_SERVICE_NAME="vardarekisterointi"
export IMAGE_TAG="organisaatio-psql-tunnel"
export DBNAME="${OPINTOPOLKU_SERVICE_NAME}"
export ECS_CLUSTER_NAME="Cluster"
export SERVICE_NAME="Bastion"
export DB_SECRET="VardaDatabaseSecret"

function main {
  export ENV="$1"; shift

  init_cloud_base_virtualenv
  export_aws_credentials "${ENV}"

  if [ "${ENV}" = "hahtuva" ]; then
    export SOURCE_ENV="hahtuva"
    export TARGET_ENV="hahtuva"
    export SOURCE_PROFILE="oph-dev"
    export SOURCE_TUNNEL_PORT="6661"
    export TARGET_TUNNEL_PORT="6662"
    source_db_hostname="${OPINTOPOLKU_SERVICE_NAME}.db.hahtuvaopintopolku.fi"
  elif [ "${ENV}" = "dev" ]; then
    export SOURCE_ENV="untuva"
    export TARGET_ENV="dev"
    export SOURCE_PROFILE="oph-dev"
    export SOURCE_TUNNEL_PORT="6663"
    export TARGET_TUNNEL_PORT="6664"
    source_db_hostname="${OPINTOPOLKU_SERVICE_NAME}.db.untuvaopintopolku.fi"
  elif [ "${ENV}" = "qa" ]; then
    export SOURCE_ENV="pallero"
    export TARGET_ENV="qa"
    export SOURCE_PROFILE="oph-dev"
    export SOURCE_TUNNEL_PORT="6665"
    export TARGET_TUNNEL_PORT="6666"
    source_db_hostname="${OPINTOPOLKU_SERVICE_NAME}.db.testiopintopolku.fi"
  elif [ "${ENV}" = "prod" ]; then
    export SOURCE_ENV="sade"
    export TARGET_ENV="prod"
    export SOURCE_PROFILE="oph-prod"
    export SOURCE_TUNNEL_PORT="6667"
    export TARGET_TUNNEL_PORT="6668"
    source_db_hostname="${OPINTOPOLKU_SERVICE_NAME}.db.opintopolku.fi"
  fi

  start_source_tunnel "${SOURCE_TUNNEL_PORT}:${source_db_hostname}:5432"
  start_target_tunnel

  do_the_heitto
}

function do_the_heitto {
  source_db_username="app"
  source_db_password="$( get_parameter "/${SOURCE_ENV}/postgresqls/${OPINTOPOLKU_SERVICE_NAME}/app-user-password" )"

  info "Fetching target password from AWS Secrets manager"
  target_username="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.username')"
  target_password="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.password')"

  cd "${repo}"
  PGPASSWORD="${source_db_password}" pg_dump --user "${source_db_username}" --host localhost --port ${SOURCE_TUNNEL_PORT} --dbname ${DBNAME} --verbose --format=custom --exclude-schema=export | \
    PGPASSWORD="${target_password}" pg_restore --user "${target_username}" --host localhost --port ${TARGET_TUNNEL_PORT} --dbname ${DBNAME} --verbose --clean --no-owner --no-privileges
}

function start_target_tunnel {
  cd "${repo}/scripts/tunnel"
  docker build --tag "${IMAGE_TAG}" .
  info "Starting tunnel from port $TARGET_TUNNEL_PORT to RDS"
  set -x
  container_id=$( docker run \
    --env ECS_CLUSTER_NAME --env SERVICE_NAME --env DB_SECRET \
    --env AWS_PROFILE --env AWS_REGION --env AWS_DEFAULT_REGION \
    --env AWS_CONTAINER_CREDENTIALS_RELATIVE_URI \
    --env AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY --env AWS_SESSION_TOKEN \
    --volume "${HOME}/.aws:/root/.aws" \
    --detach \
    --publish "$TARGET_TUNNEL_PORT:1111" \
    --name "${IMAGE_TAG}-${TARGET_ENV}" \
    --rm "${IMAGE_TAG}" )
  set +x
  trap "docker kill \"${IMAGE_TAG}-${TARGET_ENV}\"" EXIT

  docker container logs --follow "${IMAGE_TAG}-${TARGET_ENV}" &
  pid_logs=$!
  info "Waiting until ${container_id} is healthy"
  while ! is_container_healthy ${container_id}; do sleep 1; done
  kill ${pid_logs}

}

function is_container_healthy {
  local container_id="$1"
  local status="$(docker inspect --format='{{.State.Health.Status}}' ${container_id})"
  if [[ "$status" == "healthy" ]]; then
    return 0
  else
    return 1
  fi
}

function start_source_tunnel {
  local -r tunnel="$1"
  info "Starting SSH tunnel"
  # SSH keeps the connection and tunnel open until both the command executed is finished and all connections through
  # the tunnel are closed. Therefore as long as we have the psql connection open, the tunnel will stay open and close
  # automatically when all connections are closed.
  ssh -f -L "${tunnel}" "${SOURCE_ENV}-bastion" sleep 30
}

function get_parameter {
  local -r parameter_name="$1"
  aws ssm get-parameter \
    --name "${parameter_name}" \
    --with-decryption \
    --region eu-west-1 \
    --profile "${SOURCE_PROFILE}" \
    --query "Parameter.Value" \
    --output text
}

function init_cloud_base_virtualenv {
  pushd "${repo}/../cloud-base"
  info "Pulling latest cloud-base"
  git pull --rebase
  . oph-venv/bin/activate
  pip install --requirement requirements.txt > /dev/null
  popd
}

time main "$@"
