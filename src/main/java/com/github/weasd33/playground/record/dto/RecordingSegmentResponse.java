package com.github.weasd33.playground.record.dto;

import com.github.weasd33.playground.record.domain.RecordingSegment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RecordingSegmentResponse(

        @Schema(description = "녹화 세그먼트 ID", example = "1")
        Long id,

        @Schema(description = "녹화 경로 이름", example = "test")
        String pathName,

        @Schema(description = "녹화 세그먼트 파일 경로", example = "/recordings/test/2026-07-14_10-00-00-000000.mp4")
        String segmentPath,

        @Schema(description = "웹훅 수신 시각", example = "2026-07-14T10:01:00")
        LocalDateTime receivedAt
) {

    public static RecordingSegmentResponse from(RecordingSegment segment) {
        return new RecordingSegmentResponse(
                segment.getId(),
                segment.getPathName(),
                segment.getSegmentPath(),
                segment.getReceivedAt()
        );
    }
}
