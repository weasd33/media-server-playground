package com.github.weasd33.playground.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecordingToggleRequest(

        @Schema(description = "녹화 활성화 여부", example = "true")
        boolean enabled
) {
}
