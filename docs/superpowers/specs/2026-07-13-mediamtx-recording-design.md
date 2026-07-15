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

## 범위 밖 (Out of scope)

- 인증/인가 (웹훅, 제어 API 모두 무인증)
- 실제 RTSP 카메라/OBS 연동
- Testcontainers 기반 자동 통합 테스트
- 녹화 파일 재생/스트리밍, 삭제/보관 정책
