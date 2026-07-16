package com.github.weasd33.playground.record.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "recording.storage")
public record RecordingStorageProperties(

        @NotBlank(message = "recording.storage.recording-dir 설정이 필요합니다.")
        String recordingDir,

        @NotBlank(message = "recording.storage.error-dir 설정이 필요합니다.")
        String errorDir,

        @NotBlank(message = "recording.storage.completed-dir 설정이 필요합니다.")
        String completedDir
) {
}
