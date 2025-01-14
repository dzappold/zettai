#!/usr/bin/env sh

#colima start

# delete the volume if they exists
docker volume rm pg-volume || true
# create fresh volumes
docker volume create pg-volume

(docker compose --file docker-compose.yml up)
#(docker compose --file ${BASE_DIR}/docker-compose.yml up)
