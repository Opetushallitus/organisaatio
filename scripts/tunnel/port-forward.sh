#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

ssm_target="$( aws ec2 describe-instances --filters 'Name=tag:Name,Values=Bastion' 'Name=instance-state-name,Values=running' --query 'Reservations[0].Instances[0].InstanceId' --output text )"

dbhost="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.host')"
dbport="$(aws secretsmanager get-secret-value --secret-id "${DB_SECRET}" --query 'SecretString' --output text | jq -r '.port')"

# socat is used to forward port 1111 to 1112 which AWS CLI is listening on. AWS
# CLI listens on localhost:1112 so direct connection from outside the docker
# container is not possible :(
socat tcp-listen:1111,reuseaddr,fork tcp:localhost:1112 &

aws ssm start-session \
  --target "${ssm_target}" \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters "{\"host\":[\"${dbhost}\"],\"portNumber\":[\"${dbport}\"],\"localPortNumber\":[\"1112\"]}"
