#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

echo 'waiting for frontend to start'
attempt=0
until curl --output /dev/null --silent --head --fail http://localhost:3000/actuator/health; do
  echo 'waiting...'
  sleep 1
  attempt=$(( attempt + 1 ))
  if [ $attempt -gt 150 ]; then
    echo Too many attempts
    exit 1
  fi
done
echo 'frontend started'
