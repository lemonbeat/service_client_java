#!/usr/bin/env bash
set -e

RABBITMQ_USER="user"
RABBITMQ_PASS="password"
RABBITMQ_VHOST="vhost"

DEFAULT_EXCHANGES=( "PARTNER" "DMZ" "EVENT.APP" )

echo "Starting RabbitMQ server as docker container..."
docker run -d \
--name rabbit \
-p 5672:5672 \
-p 15672:15672 \
-e RABBITMQ_DEFAULT_USER=${RABBITMQ_USER} \
-e RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASS} \
-e RABBITMQ_DEFAULT_VHOST=${RABBITMQ_VHOST} \
rabbitmq:3.13.1-management

echo "Waiting for RabbitMQ server..."
sleep 10

for exchange in "${DEFAULT_EXCHANGES[@]}"
do
  echo "Declaring exchange ${exchange}..."
  docker exec -it rabbit \
  rabbitmqadmin declare exchange \
  -u ${RABBITMQ_USER} \
  -p ${RABBITMQ_PASS} \
  --vhost=${RABBITMQ_VHOST} \
  name=${exchange} \
  type=topic
done