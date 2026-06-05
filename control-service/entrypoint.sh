#!/bin/sh
set -e

echo "[entrypoint] starting"
mkdir -p /app/certs

echo "[entrypoint] IOT_CA_CERT length=${#IOT_CA_CERT}"
if [ -n "$IOT_CA_CERT" ]; then
    printf '%s' "$IOT_CA_CERT" > /app/certs/AmazonRootCA1.pem
    echo "[entrypoint] wrote AmazonRootCA1.pem"
else
    echo "[entrypoint] IOT_CA_CERT is empty, skipping"
fi

if [ -n "$IOT_CERT" ]; then
    printf '%s' "$IOT_CERT" > /app/certs/cert.pem.crt
fi
if [ -n "$IOT_KEY" ]; then
    printf '%s' "$IOT_KEY" > /app/certs/private.pem.key
fi

echo "[entrypoint] certs dir:"
ls -la /app/certs/

exec java -jar app.jar
