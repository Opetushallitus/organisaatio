#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail
source "$( dirname "${BASH_SOURCE[0]}" )/lib/common-functions.sh"

function main {
  wait_for_local_db_to_be_healthy
  select_java_version "21"
  cd "${repo}"
  ./mvnw clean install -DskipTests
  cd "${repo}/organisaatio-service"
  ../mvnw spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.config.location=classpath:application.properties,classpath:application-test-envs.properties -Dspring.profiles.active=dev -Dspring.flyway.enabled=true -Durl-virkailija=http://localhost:9000 -Dhost.virkailija=localhost:9000 -Durl-ytj=http://localhost:9000/ytj -Durl-oidservice=http://localhost:9000/oidservice -Dcas.service.organisaatio-service=http://localhost:8080/organisaatio-service-not-available"
}

function select_java_version {
  java_version="$1"
  JAVA_HOME="$(/usr/libexec/java_home -v "${java_version}")"
  export JAVA_HOME
}

function wait_for_local_db_to_be_healthy {
  wait_for_container_to_be_healthy oph-organisaatio-db
}

main "$@"
