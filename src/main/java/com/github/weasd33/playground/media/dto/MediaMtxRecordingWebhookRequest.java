package com.github.weasd33.playground.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MediaMtxRecordingWebhookRequest(

        @NotBlank(message = "pathName은 필수입니다.")
        @Schema(description = "녹화가 발생한 MediaMTX 경로 이름", example = "test")
        String pathName,

        @NotBlank(message = "segmentPath는 필수입니다.")
        @Schema(description = "생성된 녹화 세그먼트 파일 경로", example = "test_2026-07-14_10-00-00-000000.mp4")
        String segmentPath
) {
}
