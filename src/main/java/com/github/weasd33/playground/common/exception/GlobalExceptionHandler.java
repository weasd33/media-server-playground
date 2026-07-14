package com.github.weasd33.playground.common.exception;

import com.github.weasd33.playground.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 5xx 에러: 서버 측(또는 외부 인프라) 연동 장애
    @ExceptionHandler(MediaMtxApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaMtxApiException(MediaMtxApiException e) {
        log.error("[MediaMtxApiException] MediaMTX API 호출 중 서버 에러 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResponse.error(e.getMessage()));
    }

    // 4xx 에러: 클라이언트 측 잘못된 요청
    @ExceptionHandler(InvalidWebhookPayloadException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidWebhookPayloadException(InvalidWebhookPayloadException e) {
        log.warn("[InvalidWebhookPayloadException] 잘못된 웹훅 페이로드 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    // 4xx 에러: @Valid 필드 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");
        log.warn("[MethodArgumentNotValidException] 요청 값 검증 실패: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    // 예상치 못한 모든 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[UnhandledException] 예상치 못한 에러 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}
