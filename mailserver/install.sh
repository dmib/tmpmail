#!/usr/bin/env bash

mkdir -p /var/ds/mail.example.org
cd /var/ds/mail.example.org/
curl -o setup.sh https://raw.githubusercontent.com/tomav/docker-mailserver/master/setup.sh
chmod a+x ./setup.sh
ufw allow 25
ufw allow 587
ufw allow 465
docker pull tvial/docker-mailserver:latest
./setup.sh config dkim

apt install docker-compose
docker-compose up mail

./setup.sh email add admin@example.org passwd123

