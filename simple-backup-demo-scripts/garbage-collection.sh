#!/usr/bin/env bash

curl http://localhost:8080/.rest/commands/v2/simplebackup/garbage-collection \
	-H "Content-Type: application/json" \
  -X POST --user superuser:superuser \
  --data '{}'
