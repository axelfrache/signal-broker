#!/bin/sh
set -eu

CONNECT_ENDPOINT="${CONNECT_ENDPOINT:-http://kafka-connect:8083}"
CONFIG_DIR="${CONFIG_DIR:-/config}"

POSTGRES_USER="${POSTGRES_USER:-signalbroker}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-changeme}"
POSTGRES_DB="${POSTGRES_DB:-signalbroker}"

echo "==> Waiting for Kafka Connect..."
until curl -sf "${CONNECT_ENDPOINT}/connectors" >/dev/null 2>&1; do
  echo "    Kafka Connect not ready yet, retrying in 3s..."
  sleep 3
done
echo "==> Kafka Connect is ready."

for f in "${CONFIG_DIR}"/*.json; do
  [ -f "$f" ] || continue

  base="$(basename "$f")"
  case "$base" in
    sink-raw-s3.json|sink-labeled-s3.json)
      echo "==> Skipping ${base} (managed by garage-init)"
      continue
      ;;
  esac

  if [ "$base" = "sink-discord-labeled-alerts.json" ] && [ -z "${DISCORD_WEBHOOK_URL:-}" ]; then
    echo "==> Skipping ${base} (DISCORD_WEBHOOK_URL is not set)"
    continue
  fi

  rendered="/tmp/${base}"
  sed \
    -e "s|__POSTGRES_USER__|${POSTGRES_USER}|g" \
    -e "s|__POSTGRES_PASSWORD__|${POSTGRES_PASSWORD}|g" \
    -e "s|__POSTGRES_DB__|${POSTGRES_DB}|g" \
    "$f" > "$rendered"

  name="$(sed -n 's/^[[:space:]]*"name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$rendered" | head -n 1)"
  if [ -z "$name" ]; then
    echo "==> Skipping ${base} (missing connector name)"
    continue
  fi

  echo "==> Registering ${name} from ${base}"
  delete_code="$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${CONNECT_ENDPOINT}/connectors/${name}")"
  if [ "$delete_code" != "204" ] && [ "$delete_code" != "404" ]; then
    echo "    Warning: DELETE ${name} returned HTTP ${delete_code}"
  fi

  post_code="$(curl -s -o /tmp/connect-init-post.out -w "%{http_code}" \
    -X POST \
    -H "Content-Type: application/json" \
    -d @"$rendered" \
    "${CONNECT_ENDPOINT}/connectors")"
  case "$post_code" in
    2*)
      cat /tmp/connect-init-post.out
      echo
      ;;
    *)
      echo "    Warning: POST ${name} returned HTTP ${post_code}"
      cat /tmp/connect-init-post.out
      echo
      ;;
  esac
done

echo "==> Connect init complete."
