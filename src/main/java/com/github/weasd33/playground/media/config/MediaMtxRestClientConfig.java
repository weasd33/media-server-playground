package com.github.weasd33.playground.media.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class MediaMtxRestClientConfig {

    private final MediaMtxProperties mediaMtxProperties;

    @Bean
    public RestClient mediaMtxRestClient(RestClient.Builder restClientBuilder) {
        return restClientBuilder.baseUrl(mediaMtxProperties.baseUrl()).build();
    }
}
