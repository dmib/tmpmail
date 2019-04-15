#!/usr/bin/env bash

docker run \
    -p 25:25 \
    -p 8081:8081 \
    -p 443:443 \
    -p 110:110 \
    -p 143:143 \
    -p 465:465 \
    -p 587:587 \
    -p 993:993 \
    -p 995:995 \
    -e "HTTPS=OFF" \
    -e "HTTP_PORT=8081" \
    -e "DISABLE_ROUNDCUBE=TRUE"\
    -e "DISABLE_CLAMAV=TRUE" \
    -v /etc/localtime:/etc/localtime:ro \
    -v /your-data-dir/data:/data \
    -t analogic/poste.io