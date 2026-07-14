package com.github.weasd33.playground.media.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.weasd33.playground.media.dto.MediaMtxRecordingWebhookRequest;
import com.github.weasd33.playground.record.service.RecordingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MediaMtxWebhookController.class)
class MediaMtxWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordingService recordingService;

    @Test
    void 정상_웹훅_페이로드를_받으면_세그먼트를_저장한다() throws Exception {
        MediaMtxRecordingWebhookRequest request = new MediaMtxRecordingWebhookRequest("test", "/recordings/test/seg1.mp4");

        mockMvc.perform(post("/api/webhooks/mediamtx/recording")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recordingService).saveSegment(request);
    }

    @Test
    void pathName이_없으면_400을_반환하고_저장하지_않는다() throws Exception {
        String invalidPayload = """
                {"pathName":"","segmentPath":"/recordings/test/seg1.mp4"}
                """;

        mockMvc.perform(post("/api/webhooks/mediamtx/recording")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(recordingService);
    }
}
