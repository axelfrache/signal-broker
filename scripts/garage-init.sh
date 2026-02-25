#!/bin/sh
set -e

GARAGE_ADMIN_TOKEN="${GARAGE_ADMIN_TOKEN:-signal-broker-admin-token}"
GARAGE_BUCKET_NAME="${GARAGE_BUCKET_NAME:-signal-broker}"
GARAGE_ENDPOINT="http://garage:3903"
GARAGE_S3_ENDPOINT="http://garage:3900"
CONNECT_ENDPOINT="http://kafka-connect:8083"

echo "==> Waiting for Garage admin API..."
until curl -sf "${GARAGE_ENDPOINT}/health" >/dev/null 2>&1; do
  echo "    Garage not ready yet, retrying in 3s..."
  sleep 3
done
echo "==> Garage is healthy."

echo "==> Getting node ID..."
STATUS_JSON=$(curl -sf -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" "${GARAGE_ENDPOINT}/v1/status" | tr -d '\n ')
NODE_ID=$(echo "${STATUS_JSON}" | sed -n 's/.*"node":"\([^"]*\)".*/\1/p')
echo "    Node ID: ${NODE_ID}"

if [ -z "${NODE_ID}" ]; then
  echo "ERROR: Could not retrieve node ID"
  exit 1
fi

echo "==> Configuring layout (assign node to zone dc1 with 1GB capacity)..."
curl -sf -X POST \
  -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "[{\"id\":\"${NODE_ID}\",\"zone\":\"dc1\",\"capacity\":1073741824,\"tags\":[]}]" \
  "${GARAGE_ENDPOINT}/v1/layout" || true

echo ""
echo "==> Applying layout..."
LAYOUT_JSON=$(curl -sf -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" "${GARAGE_ENDPOINT}/v1/layout" | tr -d '\n ')
CURRENT_VERSION=$(echo "${LAYOUT_JSON}" | sed -n 's/.*"version":\([0-9]*\).*/\1/p')
NEXT_VERSION=$((CURRENT_VERSION + 1))
curl -sf -X POST \
  -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"version\":${NEXT_VERSION}}" \
  "${GARAGE_ENDPOINT}/v1/layout/apply" || true

echo ""
echo "==> Creating API key 'signal-broker-key'..."
KEY_RESPONSE=$(curl -sf -X POST \
  -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"name":"signal-broker-key"}' \
  "${GARAGE_ENDPOINT}/v1/key" 2>/dev/null | tr -d '\n ' || echo "")

ACCESS_KEY=$(echo "${KEY_RESPONSE}" | sed -n 's/.*"accessKeyId":"\([^"]*\)".*/\1/p')
SECRET_KEY=$(echo "${KEY_RESPONSE}" | sed -n 's/.*"secretAccessKey":"\([^"]*\)".*/\1/p')

if [ -z "${ACCESS_KEY}" ]; then
  echo "    Key may already exist, fetching existing key..."
  KEYS_LIST=$(curl -sf -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" "${GARAGE_ENDPOINT}/v1/key?list" 2>/dev/null | tr -d '\n ')
  FIRST_KEY_ID=$(echo "${KEYS_LIST}" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1)
  if [ -n "${FIRST_KEY_ID}" ]; then
    KEY_DETAIL=$(curl -sf -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" "${GARAGE_ENDPOINT}/v1/key?id=${FIRST_KEY_ID}&showSecretKey=true" 2>/dev/null | tr -d '\n ')
    ACCESS_KEY=$(echo "${KEY_DETAIL}" | sed -n 's/.*"accessKeyId":"\([^"]*\)".*/\1/p')
    SECRET_KEY=$(echo "${KEY_DETAIL}" | sed -n 's/.*"secretAccessKey":"\([^"]*\)".*/\1/p')
  fi
fi

echo "    Access Key: ${ACCESS_KEY}"
echo "    Secret Key: ${SECRET_KEY}"

echo "==> Creating bucket '${GARAGE_BUCKET_NAME}'..."
curl -sf -X POST \
  -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"globalAlias\":\"${GARAGE_BUCKET_NAME}\"}" \
  "${GARAGE_ENDPOINT}/v1/bucket" 2>/dev/null || echo "(bucket may already exist)"

echo ""
echo "==> Getting bucket ID..."
BUCKET_ID=$(curl -sf -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" "${GARAGE_ENDPOINT}/v1/bucket?globalAlias=${GARAGE_BUCKET_NAME}" 2>/dev/null | tr -d '\n ' | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "    Bucket ID: ${BUCKET_ID}"

if [ -n "${ACCESS_KEY}" ] && [ -n "${BUCKET_ID}" ]; then
  echo "==> Granting key read+write access to bucket..."
  curl -sf -X POST \
    -H "Authorization: Bearer ${GARAGE_ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"bucketId\":\"${BUCKET_ID}\",\"accessKeyId\":\"${ACCESS_KEY}\",\"permissions\":{\"read\":true,\"write\":true,\"owner\":true}}" \
    "${GARAGE_ENDPOINT}/v1/bucket/allow" 2>/dev/null || true
  echo ""
fi

echo "==> Waiting for Kafka Connect..."
until curl -sf "${CONNECT_ENDPOINT}/connectors" >/dev/null 2>&1; do
  echo "    Kafka Connect not ready yet, retrying in 5s..."
  sleep 5
done
echo "==> Kafka Connect is ready."

echo "==> Registering S3 Sink connectors with Garage credentials..."

cat <<EOF > /tmp/sink-labeled-s3.json
{
    "name": "sink-labeled-s3",
    "config": {
        "connector.class": "io.confluent.connect.s3.S3SinkConnector",
        "tasks.max": "1",
        "topics": "support.labeled",
        "s3.bucket.name": "${GARAGE_BUCKET_NAME}",
        "topics.dir": "labeled",
        "s3.region": "garage",
        "store.url": "${GARAGE_S3_ENDPOINT}",
        "aws.access.key.id": "${ACCESS_KEY}",
        "aws.secret.access.key": "${SECRET_KEY}",
        "s3.part.size": "5242880",
        "flush.size": "100",
        "rotate.interval.ms": "3600000",
        "storage.class": "io.confluent.connect.s3.storage.S3Storage",
        "format.class": "io.confluent.connect.s3.format.json.JsonFormat",
        "partitioner.class": "io.confluent.connect.storage.partitioner.TimeBasedPartitioner",
        "path.format": "'year'=YYYY/'month'=MM/'day'=dd/'hour'=HH",
        "locale": "en-US",
        "timezone": "UTC",
        "timestamp.extractor": "Wallclock",
        "partition.duration.ms": "3600000",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "value.converter": "io.confluent.connect.json.JsonSchemaConverter",
        "value.converter.schema.registry.url": "http://schema-registry:8081",
        "schema.compatibility": "NONE"
    }
}
EOF

cat <<EOF > /tmp/sink-raw-s3.json
{
    "name": "sink-raw-s3",
    "config": {
        "connector.class": "io.confluent.connect.s3.S3SinkConnector",
        "tasks.max": "1",
        "topics": "support.mail.raw,support.whatsapp.raw",
        "s3.bucket.name": "${GARAGE_BUCKET_NAME}",
        "topics.dir": "raw",
        "s3.region": "garage",
        "store.url": "${GARAGE_S3_ENDPOINT}",
        "aws.access.key.id": "${ACCESS_KEY}",
        "aws.secret.access.key": "${SECRET_KEY}",
        "s3.part.size": "5242880",
        "flush.size": "100",
        "rotate.interval.ms": "3600000",
        "storage.class": "io.confluent.connect.s3.storage.S3Storage",
        "format.class": "io.confluent.connect.s3.format.json.JsonFormat",
        "partitioner.class": "io.confluent.connect.storage.partitioner.TimeBasedPartitioner",
        "path.format": "'year'=YYYY/'month'=MM/'day'=dd/'hour'=HH",
        "locale": "en-US",
        "timezone": "UTC",
        "timestamp.extractor": "Wallclock",
        "partition.duration.ms": "3600000",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter.schemas.enable": "false",
        "schema.compatibility": "NONE"
    }
}
EOF

echo "==> Registering sink-labeled-s3..."
curl -sf -X POST \
  -H "Content-Type: application/json" \
  -d @/tmp/sink-labeled-s3.json \
  "${CONNECT_ENDPOINT}/connectors" || echo "(may already exist)"
echo ""

echo "==> Registering sink-raw-s3..."
curl -sf -X POST \
  -H "Content-Type: application/json" \
  -d @/tmp/sink-raw-s3.json \
  "${CONNECT_ENDPOINT}/connectors" || echo "(may already exist)"
echo ""

echo "============================================"
echo "==> Garage init complete!"
echo "    Bucket: ${GARAGE_BUCKET_NAME}"
echo "    S3 Endpoint: ${GARAGE_S3_ENDPOINT}"
echo "    Access Key: ${ACCESS_KEY}"
echo "    Connectors registered with Kafka Connect."
echo "============================================"
