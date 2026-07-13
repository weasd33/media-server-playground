FROM bluenviron/mediamtx:latest AS mediamtx

FROM alpine:3.20
RUN apk add --no-cache curl
COPY --from=mediamtx /mediamtx /mediamtx
COPY mediamtx.yml /mediamtx.yml
COPY hooks/notify-segment-complete.sh /hooks/notify-segment-complete.sh
RUN chmod +x /hooks/notify-segment-complete.sh
ENTRYPOINT ["/mediamtx"]
