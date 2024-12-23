#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

function main {
  select_java_version "21"
  wait_for_local_db_to_be_healthy

  VALTUUDET_API_KEY="dummy"
  VALTUUDET_CLIENT_ID="dummy"
  VALTUUDET_OAUTH_PASSWORD="dummy"
  SERVICE_USERNAME="dummy"
  SERVICE_PASSWORD="dummy"

  mvn spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="
      -Dvarda-rekisterointi.service.username=$SERVICE_USERNAME
      -Dvarda-rekisterointi.service.password=$SERVICE_PASSWORD
      -Dvarda-rekisterointi.valtuudet.api-key=$VALTUUDET_API_KEY
      -Dvarda-rekisterointi.valtuudet.client-id=$VALTUUDET_CLIENT_ID
      -Dvarda-rekisterointi.valtuudet.oauth-password=$VALTUUDET_OAUTH_PASSWORD
      -Dvarda-rekisterointi.url-virkailija=https://virkailija.untuvaopintopolku.fi"
}

function select_java_version {
  java_version="$1"
  JAVA_HOME="$(/usr/libexec/java_home --failfast --version "${java_version}" > /dev/null 2>&1 || fatal "JDK version ${java_version} required but not installed")"
  export JAVA_HOME
}

function wait_for_local_db_to_be_healthy {
  wait_for_container_to_be_healthy varda-rekisterointi-db
}

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

main
