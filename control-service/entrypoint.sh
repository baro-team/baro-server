#!/bin/sh
set -e

mkdir -p /app/certs
if [ -n "$IOT_CA_CERT" ]; then
    printf '%s' "$IOT_CA_CERT" > /app/certs/AmazonRootCA1.pem
fi
if [ -n "$IOT_CERT" ]; then
    printf '%s' "$IOT_CERT" > /app/certs/cert.pem.crt
fi
if [ -n "$IOT_KEY" ]; then
    printf '%s' "$IOT_KEY" > /app/certs/private.pem.key
fi

exec java -jar app.jar
