package com.github.weasd33.playground.media.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "mediamtx")
public record MediaMtxProperties(

        @NotBlank(message = "mediamtx.base-url 설정이 필요합니다.")
        String baseUrl
) {
}
