#!/usr/bin/env bash
set -euo pipefail
cd /opt/yarumito
git fetch origin
git checkout master
git reset --hard origin/master
docker compose -f docker-compose.prod.yml build backend
docker compose -f docker-compose.prod.yml build frontend
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml ps
