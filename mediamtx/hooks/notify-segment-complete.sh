#!/bin/sh
curl -s -X POST "http://host.docker.internal:8080/api/webhooks/mediamtx/recording" \
  -H "Content-Type: application/json" \
  -d "{\"pathName\":\"$MTX_PATH\",\"segmentPath\":\"$MTX_SEGMENT_PATH\"}"
