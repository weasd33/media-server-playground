package com.github.weasd33.playground.media.client;

import com.github.weasd33.playground.common.exception.MediaMtxApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class MediaMtxApiClient {

    private static final String PATH_PATCH_URI = "/v3/config/paths/patch/{pathName}";

    private final RestClient restClient;

    public void setRecording(String pathName, boolean enabled) {
        try {
            restClient.patch()
                    .uri(PATH_PATCH_URI, pathName)
                    .body(new MediaMtxPathPatchRequest(enabled))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new MediaMtxApiException("MediaMTX 녹화 설정 변경에 실패했습니다. pathName=" + pathName, e);
        }
    }
}
