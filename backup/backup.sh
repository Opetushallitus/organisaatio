#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

readonly dump_dir="$(mktemp --directory)"
readonly dump_file="${dump_dir}/dump"

function cleanup {
  echo "Deleting dump file $dump_file"
  rm -f "$dump_file"
}

trap cleanup EXIT

function main {
  local seconds_from_epoch
  seconds_from_epoch=$(date +%s)

  ensure_daily_backup_stored_in_s3 "$seconds_from_epoch"
  ensure_monthly_backup_stored_in_s3 "$seconds_from_epoch"
}

function create_dump {
  PGPASSWORD="$DB_PASSWORD" pg_dump \
    --user "$DB_USERNAME" \
    --host $DB_HOSTNAME \
    --port $DB_PORT \
    --dbname $DB_NAME \
    --format custom \
    --file "$dump_file"
}

function ensure_daily_backup_stored_in_s3 {
  local seconds_from_epoch=$1
  ensure_backup_is_stored_in_s3 "$S3_BUCKET" "$DB_NAME" "daily" "$seconds_from_epoch"
}

function ensure_monthly_backup_stored_in_s3 {
  local seconds_from_epoch=$1
  ensure_backup_is_stored_in_s3 "$S3_BUCKET" "$DB_NAME" "monthly" "$seconds_from_epoch"
}

function ensure_backup_is_stored_in_s3 {
  local bucket=$1
  local dbname=$2
  local frequency=$3
  local seconds_from_epoch=$4
  local date_format
  date_format=$(date_format_for_frequency "$frequency")
  local formatted_date
  formatted_date=$(date -d @"$seconds_from_epoch" "$date_format")
  local key="$frequency/$dbname/$formatted_date-$dbname.dump"

  echo "Checking whether key $key exists in bucket $bucket"
  if ! key_exists_in_bucket "$bucket" "$key"; then
    echo "Key $key does not exist in bucket $bucket"
    echo "Copying dump to bucket $bucket with key $key"
    copy_dump_to_bucket "$bucket" "$key"
  else
    echo "Key $key exists in bucket $bucket"
  fi

  echo "Checking size of object with key $key in bucket $bucket"
  local size
  size=$(size_of_object_in_bucket "$bucket" "$key")
  echo "Object with key $key in bucket $bucket has size $size"

  local timestamp
  timestamp=$(date +"%Y-%m-%dT%H:%M:%S%z")
  echo "{\"timestamp\": \"$timestamp\", \"size\": \"$size\", \"dbname\": \"$dbname\", \"frequency\": \"$frequency\", \"bucket\": \"$bucket\", \"key\": \"$key\"}"
}

function date_format_for_frequency {
  local frequency=$1

  case "$frequency" in
      "daily")
          echo "+%Y-%m-%d"
          ;;
      "monthly")
          echo "+%Y-%m"
          ;;
      *)
          echo "Unknown frequency."
          return 1
          ;;
  esac
}

function copy_dump_to_bucket {
  local bucket=$1
  local key=$2
  local url=$(s3_url $bucket $key)

  ensure_dump_created

  aws s3 cp "$dump_file" "$url"
}

function ensure_dump_created {
  echo "Checking if dump $dump_file already exists"
  if ! ls "$dump_file"; then
    echo "Creating dump $dump_file"
    create_dump
  else
    echo "Dump $dump_file already exists"
  fi
}

function key_exists_in_bucket {
  local bucket=$1
  local key=$2

  aws s3 ls $(s3_url $bucket $key) > /dev/null 2>&1
}

function size_of_object_in_bucket {
  local bucket=$1
  local key=$2
  local output=$(aws s3api head-object --bucket "$bucket" --key "$key" 2>&1 || true)
  local size

  if echo "$output" | grep -q 'ContentLength'; then
    size=$(echo "$output" | jq -r '.ContentLength')
  else
    size=0
  fi

  echo $size
}

function s3_url {
  local bucket=$1
  local key=$2

  echo "s3://$bucket/$key"
}

main