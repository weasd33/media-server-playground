
# 구현 계획

## MediaMTX 녹화 연동

### 인프라
- [x] `mediamtx/Dockerfile` 작성 (alpine + curl 위에 mediamtx 바이너리 복사)
- [x] `mediamtx/mediamtx.yml` 작성 (`api`, `paths.test`, `record`, `runOnRecordSegmentComplete`)
- [x] `mediamtx/hooks/notify-segment-complete.sh` 작성
- [x] `docker-compose.yml` 작성 (`mediamtx`, `postgres` 서비스)
- [x] `.env.example` 작성(`POSTGRES_DB`/`POSTGRES_USER`/`POSTGRES_PASSWORD`) 및 `.env`를 `.gitignore`에 추가
- [x] `scripts/push-test-stream.ps1` 작성 (ffmpeg 테스트 스트림 push)

### media 도메인 (MediaMTX 연동)
- [x] `media/config/MediaMtxProperties` 작성
- [x] `media/client/MediaMtxApiClient` 작성 (recording on/off 제어 API 호출)
- [x] `media/dto/MediaMtxRecordingWebhookRequest` 작성
- [x] `media/controller/MediaMtxWebhookController` 작성

### record 도메인 (녹화 제어/조회)
- [x] `record/domain/RecordingSegment` (JPA Entity) 작성
- [x] `record/repository/RecordingSegmentRepository` 작성
- [x] `record/dto/RecordingToggleRequest`, `record/dto/RecordingSegmentResponse` 작성
- [x] `record/service/RecordingService` 작성
- [x] `record/controller/RecordingController` 작성

### 공통
- [x] `common/response/ApiResponse` 작성
- [x] `common/exception/MediaMtxApiException`, `common/exception/InvalidWebhookPayloadException` 작성
- [x] `common/exception/GlobalExceptionHandler` 작성

### 설정
- [x] `application.yml`에 PostgreSQL datasource(`.env` 값 참조), JPA 설정 추가
- [x] SpringDoc(Swagger) 의존성 추가 및 각 엔드포인트/DTO에 `@Operation`/`@Schema` 작성

### 테스트
- [x] `RecordingController` `@WebMvcTest` 작성
- [x] `MediaMtxWebhookController` `@WebMvcTest` 작성
- [x] `RecordingService` 단위 테스트 작성 (`MediaMtxApiClient` mock)

### 수동 검증
- [x] `docker compose up`으로 mediamtx/postgres 기동 확인
- [x] `PATCH /api/paths/test/recording`으로 녹화 on 확인
- [x] `scripts/push-test-stream.ps1`으로 테스트 스트림 송출
- [x] `mediamtx/recordings` 디렉토리에 세그먼트 파일 생성 확인
- [x] `GET /api/recordings`로 메타데이터 저장 확인

## 녹화 웹훅 신뢰성 개선

### MediaMTX 설정/훅
- [x] `mediamtx/mediamtx.yml`의 `recordPath`를 평평한 구조로 변경 (`recordings/recording/%path_%Y-%m-%d_%H-%M-%S-%f`)
- [x] `mediamtx/hooks/notify-segment-complete.sh` 재작성 (웹훅 시도 후 응답 코드로 `completed`/`error` 분기, `--max-time` 추가)

### media 도메인
- [x] `media/dto/MediaMtxRecordingWebhookRequest`의 `segmentPath` 예시 값을 파일명 기준으로 수정

### record 도메인
- [x] `record/domain/RecordingSegment`에 `segmentPath` unique 제약 추가
- [x] `record/repository/RecordingSegmentRepository`에 `existsBySegmentPath` 추가
- [x] `record/config/RecordingStorageProperties` 작성 (`error`/`completed` base 디렉토리 설정화)
- [x] `record/scheduler/RecordingReconciliationScheduler` 작성 (`error/` 스캔 → DB 저장 → `completed/` 이동)
- [x] `MediaPlaygroundApplication`에 `@EnableScheduling` 추가

### 설정
- [x] `application.yml`에 `recording.storage.error-dir`/`completed-dir` 추가

### 테스트
- [x] `RecordingReconciliationScheduler` 단위 테스트 작성 (`@TempDir` + Repository mock)
- [x] `RecordingSegment` unique 제약 검증 `@DataJpaTest` 작성

### 수동 검증
- [x] Spring Boot 중지 상태에서 세그먼트 녹화 → `error/`에 파일 쌓이는지 확인
- [x] Spring Boot 재기동 후 재조정 스케줄러가 DB 저장 + `completed/` 이동시키는지 확인
- [x] 정상 상황에서 웹훅 성공 시 `completed/`로 바로 이동하는지 확인
