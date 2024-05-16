#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

function main {
  select_java_version "21"

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
