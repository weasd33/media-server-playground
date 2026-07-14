
# 구현 계획

프로젝트에서 구현할 기능 단위를 체크박스로 나열하고, 하나씩 구현할 때마다 체크한다. 각 주제의 상세 아키텍처/설계는 `docs/superpowers/specs/`의 개별 설계 문서를 참고한다.

## MediaMTX 녹화 연동

> 설계 문서: `docs/superpowers/specs/2026-07-13-mediamtx-recording-design.md`

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
- [ ] `docker compose up`으로 mediamtx/postgres 기동 확인
- [ ] `PATCH /api/paths/test/recording`으로 녹화 on 확인
- [ ] `scripts/push-test-stream.ps1`으로 테스트 스트림 송출
- [ ] `mediamtx/recordings` 디렉토리에 세그먼트 파일 생성 확인
- [ ] `GET /api/recordings`로 메타데이터 저장 확인
