#!/bin/sh

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Error: Directory arguments are missing."
  exit 1
fi

COMPLETED_DIR="$1"
ERROR_DIR="$2"
FILE_NAME=$(basename "$MTX_SEGMENT_PATH")

mkdir -p "$COMPLETED_DIR" "$ERROR_DIR"

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 \
  -X POST "http://host.docker.internal:8080/api/webhooks/mediamtx/recording" \
  -H "Content-Type: application/json" \
  -d "{\"pathName\":\"$MTX_PATH\",\"segmentPath\":\"$FILE_NAME\"}")

case "$HTTP_CODE" in
  2??)
    mv "$MTX_SEGMENT_PATH" "$COMPLETED_DIR/$FILE_NAME"
    ;;
  *)
    mv "$MTX_SEGMENT_PATH" "$ERROR_DIR/$FILE_NAME"
    ;;
esac
