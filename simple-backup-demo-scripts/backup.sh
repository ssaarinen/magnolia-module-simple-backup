#!/usr/bin/env bash

mkdir -p /tmp/mgnl-demo-backup

curl http://localhost:8080/.rest/commands/v2/simplebackup/backup \
	-H "Content-Type: application/json" \
  -X POST --user superuser:superuser \
  --data \
'{
  "configuration": "demo",
  "backup-subdirectory": "demo-backup"
}'
