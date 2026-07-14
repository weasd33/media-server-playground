package com.github.weasd33.playground.record.controller;

import com.github.weasd33.playground.common.response.ApiResponse;
import com.github.weasd33.playground.record.dto.RecordingSegmentResponse;
import com.github.weasd33.playground.record.dto.RecordingToggleRequest;
import com.github.weasd33.playground.record.service.RecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecordingController {

    private final RecordingService recordingService;

    @Operation(summary = "녹화 on/off 토글", description = "MediaMTX 특정 경로의 녹화를 활성화/비활성화한다.")
    @PatchMapping("/paths/{pathName}/recording")
    public ApiResponse<Void> toggleRecording(
            @Parameter(description = "MediaMTX 경로 이름", example = "test") @PathVariable String pathName,
            @RequestBody RecordingToggleRequest request
    ) {
        recordingService.toggleRecording(pathName, request.enabled());
        return ApiResponse.success(null);
    }

    @Operation(summary = "녹화 세그먼트 목록 조회", description = "저장된 녹화 세그먼트 메타데이터를 조회한다. pathName으로 필터링할 수 있다.")
    @GetMapping("/recordings")
    public ApiResponse<List<RecordingSegmentResponse>> getRecordings(
            @Parameter(description = "필터링할 경로 이름", example = "test") @RequestParam(required = false) String pathName
    ) {
        return ApiResponse.success(recordingService.getSegments(pathName));
    }
}
