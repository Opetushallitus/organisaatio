#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  select_java_version "21"
  wait_for_local_db_to_be_healthy

  VALTUUDET_API_KEY="dummy"
  VALTUUDET_CLIENT_ID="dummy"
  VALTUUDET_OAUTH_PASSWORD="dummy"
  SERVICE_USERNAME="dummy"
  SERVICE_PASSWORD="dummy"

  cd "${repo}/varda-rekisterointi"
  ../mvnw spring-boot:run \
    -Dspring-boot.run.profiles=dev \
    -Dspring-boot.run.jvmArguments="
      -Dvarda-rekisterointi.service.username=$SERVICE_USERNAME
      -Dvarda-rekisterointi.service.password=$SERVICE_PASSWORD
      -Dvarda-rekisterointi.palvelukayttaja.client-id=dummy
      -Dvarda-rekisterointi.palvelukayttaja.client-secret=dummy
      -Dotuva.jwt.issuer-uri=http://localhost:9000
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

main "$@"