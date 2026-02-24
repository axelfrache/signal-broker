#!/bin/bash
set -e

CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Waiting for Kafka Connect to be ready..."
until curl -s "$CONNECT_URL/connectors" > /dev/null 2>&1; do
  sleep 2
done
echo "Kafka Connect is ready."

for config_file in "$SCRIPT_DIR"/*.json; do
  connector_name=$(jq -r '.name' "$config_file")
  echo "Registering connector: $connector_name"
  curl -s -X PUT \
    "$CONNECT_URL/connectors/$connector_name/config" \
    -H "Content-Type: application/json" \
    -d "$(jq '.config' "$config_file")" | jq .
  echo ""
done

echo "All connectors registered."
