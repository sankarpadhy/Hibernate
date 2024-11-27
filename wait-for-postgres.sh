#!/bin/sh
# wait-for-postgres.sh

set -e

host="$1"
shift

until PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h "$host" -U "$SPRING_DATASOURCE_USERNAME" -d "$SPRING_DATASOURCE_DATABASE" -c '\q'; do
  >&2 echo "Postgres is unavailable - sleeping"
  sleep 1
done

>&2 echo "Postgres is up - executing command"
exec "$@"
