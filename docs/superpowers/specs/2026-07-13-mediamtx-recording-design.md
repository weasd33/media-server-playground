# MediaMTX 녹화 연동 설계

## 배경 및 목표

이 프로젝트는 영상 처리/스트리밍 관련 기술(FFmpeg, MediaMTX, SRS, OME, LiveKit, mediasoup 등)을 실습하기 위한 플레이그라운드다. 첫 실습 주제로 **MediaMTX를 이용한 녹화 처리 프로세스**를 다룬다.

목표:
- MediaMTX를 Docker로 띄우고 RTSP push 스트림을 녹화하는 기본 흐름을 익힌다.
- Spring Boot 애플리케이션에서 MediaMTX Control API를 호출해 경로별 녹화를 on/off 제어한다.
- MediaMTX의 recording 훅(`runOnRecordSegmentComplete`)을 통해 녹화 세그먼트 완료 이벤트를 Spring Boot 웹훅으로 수신하고, 메타데이터를 DB에 기록한다.

## 아키텍처

```
ffmpeg(로컬)  --RTSP push-->  MediaMTX(Docker, 8554/RTSP, 9997/API)
                                   |  runOnRecordSegmentComplete (hook script → curl)
                                   v
                          Spring Boot App(8080, 로컬 실행)
                          - Webhook 수신 → PostgreSQL 저장
                          - REST API로 MediaMTX Control API 호출 (recording on/off)
```

- MediaMTX는 `docker-compose.yml`로 실행한다. 공식 `bluenviron/mediamtx` 이미지는 scratch 기반이라 셸/`curl`이 없어 훅 스크립트를 실행할 수 없으므로, `mediamtx/Dockerfile`에서 mediamtx 바이너리를 alpine(+curl) 위에 올린 커스텀 이미지를 빌드해 사용한다.
- PostgreSQL도 같은 `docker-compose.yml`에서 실행한다(로컬 개발용 DB).
- Spring Boot 앱은 Docker 밖에서 `./gradlew bootRun`으로 로컬 실행하며, PostgreSQL은 `localhost:5432`로 접근한다.
- MediaMTX 컨테이너 → 호스트의 Spring Boot 접근은 `host.docker.internal`을 사용한다(`docker-compose.yml`에 `extra_hosts: host.docker.internal:host-gateway` 추가로 Linux 호환성도 확보).

## 컴포넌트

### 1. `docker-compose.yml`

```yaml
services:
  mediamtx:
    build: ./mediamtx
    container_name: mediamtx
    ports:
      - "8554:8554"   # RTSP
      - "9997:9997"   # API
    volumes:
      - ./mediamtx/recordings:/recordings
    extra_hosts:
      - "host.docker.internal:host-gateway"

  postgres:
    image: postgres:16-alpine
    container_name: media-playground-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

민감 정보(`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`)는 `.env` 파일로 관리하며 `.gitignore`에 포함한다. `application.yml`의 `spring.datasource` 설정도 동일한 값을 환경변수로 참조한다.

### 2. `mediamtx/Dockerfile`

```dockerfile
FROM bluenviron/mediamtx:latest AS mediamtx

FROM alpine:3.20
RUN apk add --no-cache curl
COPY --from=mediamtx /mediamtx /mediamtx
COPY mediamtx.yml /mediamtx.yml
COPY hooks/notify-segment-complete.sh /hooks/notify-segment-complete.sh
RUN chmod +x /hooks/notify-segment-complete.sh
ENTRYPOINT ["/mediamtx"]
```

### 3. `mediamtx/mediamtx.yml`

```yaml
api: yes
apiAddress: :9997

authInternalUsers:
  - user: any
    pass:
    ips: []
    permissions:
      - action: publish
      - action: read
      - action: playback
      - action: api
      - action: metrics

paths:
  test:
    source: publisher
    record: no
    recordPath: /recordings/%path/%Y-%m-%d_%H-%M-%S-%f
    recordFormat: fmp4
    recordSegmentDuration: 1m
    runOnRecordSegmentComplete: /hooks/notify-segment-complete.sh
```

`record: no`로 시작하며, Spring Boot API 호출로 켠다(v3 Control API `PATCH /v3/config/paths/patch/test` body `{"record": true}`).

기본 `mediamtx.yml`은 `api` 액션을 루프백(127.0.0.1)에서만 허용하는데, 호스트에서 게시된 포트로 접근하면 컨테이너 입장에서는 루프백이 아니라 인증 오류가 난다. 학습용 환경이므로 `authInternalUsers`에 `api`/`metrics` 액션을 인증 없이 허용하도록 명시한다(운영 환경에서는 부적절하므로 범위 밖으로 둔다).

### 4. `mediamtx/hooks/notify-segment-complete.sh`

```sh
#!/bin/sh
curl -s -X POST "http://host.docker.internal:8080/api/webhooks/mediamtx/recording" \
  -H "Content-Type: application/json" \
  -d "{\"pathName\":\"$MTX_PATH\",\"segmentPath\":\"$MTX_SEGMENT_PATH\"}"
```

### 5. `scripts/push-test-stream.ps1`

ffmpeg lavfi testsrc를 `rtsp://localhost:8554/test`로 push하는 로컬 실행용 PowerShell 스크립트(수동 실행, 자동화하지 않음).

```powershell
ffmpeg -re -f lavfi -i "testsrc=size=640x480:rate=25" `
  -f lavfi -i "sine=frequency=1000" `
  -c:v libx264 -preset veryfast -c:a aac `
  -rtsp_transport tcp `
  -f rtsp rtsp://localhost:8554/test
```

`docker-compose.yml`은 RTSP 시그널링 포트(8554/TCP)만 노출하고 UDP RTP/RTCP 포트(8000-8001)는 노출하지 않으므로, 기본 UDP 트랜스포트로 publish하면 RTCP를 못 받아 세션이 타임아웃된다. `-rtsp_transport tcp`로 미디어까지 TCP 하나로 터널링해서 이 문제를 피한다.

### 6. Spring Boot 패키지 구조 (도메인 계층형)

```
com.github.weasd33.playground
├── record/                                  # 녹화 도메인 (사용자 대상 제어/조회, 저장)
│   ├── controller/RecordingController.java
│   ├── service/RecordingService.java
│   ├── domain/RecordingSegment.java         # JPA Entity
│   ├── repository/RecordingSegmentRepository.java
│   ├── dto/RecordingToggleRequest.java
│   └── dto/RecordingSegmentResponse.java
├── media/                                   # MediaMTX 연동 도메인 (API 클라이언트, 웹훅 수신)
│   ├── controller/MediaMtxWebhookController.java
│   ├── client/MediaMtxApiClient.java        # RestClient 기반 MediaMTX v3 Control API 래퍼
│   ├── config/MediaMtxProperties.java       # application.yml의 mediamtx.* 바인딩
│   └── dto/MediaMtxRecordingWebhookRequest.java
└── common/                                  # 공통 응답/예외 처리
    ├── response/ApiResponse.java
    ├── exception/MediaMtxApiException.java
    ├── exception/InvalidWebhookPayloadException.java
    └── exception/GlobalExceptionHandler.java
```

의존 방향: `record.controller` → `record.service` → `media.client`(녹화 on/off 제어), `media.controller`(웹훅) → `record.service`(세그먼트 저장). `common`은 양쪽 도메인에서 공용으로 참조한다.

### 7. 저장소

런타임/개발 DB는 PostgreSQL(docker-compose로 실행)을 사용하고, H2는 테스트(`@DataJpaTest` 등)에서만 인메모리로 사용한다. Spring Data JPA + QueryDSL을 사용한다.

`RecordingSegment` 필드: `id`, `pathName`, `segmentPath`, `receivedAt`(웹훅 수신 시각, 서버에서 채움).

### 8. DTO 및 API 문서화

Request/Response DTO는 Java `record`로 작성한다.

- `RecordingToggleRequest(boolean enabled)`
- `RecordingSegmentResponse(Long id, String pathName, String segmentPath, LocalDateTime receivedAt)`
- `MediaMtxRecordingWebhookRequest(String pathName, String segmentPath)`

각 Controller 엔드포인트에는 `@Operation`, 각 DTO 필드에는 `@Schema(example, description)`을 작성한다.

### 9. 에러 응답 / 예외 처리

커스텀 예외 + `@RestControllerAdvice`로 공통 `ApiResponse` 포맷을 사용한다.

- `MediaMtxApiException` — MediaMTX Control API 호출 실패 시 (→ 502)
- `InvalidWebhookPayloadException` — 웹훅 payload 필수 필드 누락 시 (→ 400)
- `GlobalExceptionHandler`(`@RestControllerAdvice`)에서 두 예외를 공통 `ApiResponse` 포맷으로 변환

### 10. 동시성 고려

이 기능은 단일 테스트 경로(`test`)를 대상으로 한 수동 on/off 토글과 웹훅 저장(단순 insert)이라 동시 갱신 충돌 가능성이 낮다고 판단해 별도의 분산 락/DB 락은 적용하지 않는다. 이후 경로가 여러 개로 늘어나거나 동시 toggle이 문제가 되면 재검토한다.

## API 명세

| Method | Path | 설명 |
|---|---|---|
| PATCH | `/api/paths/{pathName}/recording` | body `RecordingToggleRequest` — MediaMTX 해당 경로 녹화 on/off |
| GET | `/api/recordings` | 저장된 녹화 세그먼트 전체 조회, 응답은 `RecordingSegmentResponse` 목록 (쿼리파라미터 `pathName`으로 필터 가능) |
| POST | `/api/webhooks/mediamtx/recording` | MediaMTX 훅이 호출하는 내부용 웹훅, body `MediaMtxRecordingWebhookRequest` |

모든 응답은 공통 `ApiResponse` 포맷으로 감싼다.

## 데이터 흐름

1. 사용자가 `PATCH /api/paths/test/recording {"enabled": true}` 호출 → `RecordingService`가 `MediaMtxApiClient`로 MediaMTX Control API 호출 → `record: true` 반영
2. `scripts/push-test-stream.ps1` 실행 → ffmpeg가 `rtsp://localhost:8554/test`로 테스트 스트림 push
3. MediaMTX가 `recordSegmentDuration`(1분)마다 녹화 파일을 `/recordings`에 기록
4. 세그먼트 완료 시 `notify-segment-complete.sh`가 실행되어 Spring Boot 웹훅으로 `pathName`, `segmentPath` POST
5. `MediaMtxWebhookController` → `RecordingService.saveSegment(...)` → `RecordingSegmentRepository`에 저장
6. 사용자가 `GET /api/recordings`로 조회하거나 PostgreSQL에 직접 접속해 확인

## 에러 처리 (범위 최소화)

- MediaMTX API 호출 실패(연결 불가, 4xx/5xx) 시 `MediaMtxApiClient`가 `MediaMtxApiException`을 던지고, `GlobalExceptionHandler`가 502로 변환해 응답한다. 재시도 로직은 넣지 않는다(학습용 스코프).
- 웹훅 payload에 필수 필드(`pathName`, `segmentPath`)가 없으면 `InvalidWebhookPayloadException`을 던지고 400으로 변환, 로그로 남긴다.
- MediaMTX 컨테이너 다운/네트워크 문제는 이번 스코프에서 자동 복구 대상이 아니다(수동 `docker compose restart`).

## 테스트 전략

- `RecordingController`, `MediaMtxWebhookController`: `@WebMvcTest` + `MockMvc`로 요청/응답 검증
- `RecordingService`: 단위 테스트로 `MediaMtxApiClient`를 mock 처리해 로직 검증
- MediaMTX/ffmpeg 연동 자체는 자동화하지 않고 수동 검증: `docker compose up` → 녹화 API 호출 → ffmpeg push → `/recordings` 디렉토리 및 `/api/recordings` 응답으로 확인

## 범위 밖 (Out of scope)

- 인증/인가 (웹훅, 제어 API 모두 무인증)
- 실제 RTSP 카메라/OBS 연동
- Testcontainers 기반 자동 통합 테스트
- 녹화 파일 재생/스트리밍, 삭제/보관 정책
