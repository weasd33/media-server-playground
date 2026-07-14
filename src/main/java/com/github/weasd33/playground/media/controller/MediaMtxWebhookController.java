package com.github.weasd33.playground.media.controller;

import com.github.weasd33.playground.common.response.ApiResponse;
import com.github.weasd33.playground.media.dto.MediaMtxRecordingWebhookRequest;
import com.github.weasd33.playground.record.service.RecordingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/webhooks/mediamtx")
public class MediaMtxWebhookController {

    private final RecordingService recordingService;

    @Operation(
            summary = "MediaMTX 녹화 세그먼트 완료 웹훅",
            description = "MediaMTX의 runOnRecordSegmentComplete 훅이 호출하는 내부용 엔드포인트로, 완료된 녹화 세그먼트 메타데이터를 저장한다."
    )
    @PostMapping("/recording")
    public ApiResponse<Void> receiveRecordingSegment(@Valid @RequestBody MediaMtxRecordingWebhookRequest request) {
        recordingService.saveSegment(request);
        return ApiResponse.success(null);
    }
}
