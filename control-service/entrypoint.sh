#!/bin/sh
set -e

mkdir -p /app/certs
printf '%s' "$IOT_CA_CERT" > /app/certs/AmazonRootCA1.pem
printf '%s' "$IOT_CERT"    > /app/certs/cert.pem.crt
printf '%s' "$IOT_KEY"     > /app/certs/private.pem.key

exec java -jar app.jar
