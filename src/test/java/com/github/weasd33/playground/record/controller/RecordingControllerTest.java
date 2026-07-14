package com.github.weasd33.playground.record.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.weasd33.playground.record.dto.RecordingSegmentResponse;
import com.github.weasd33.playground.record.dto.RecordingToggleRequest;
import com.github.weasd33.playground.record.service.RecordingService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecordingController.class)
class RecordingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecordingService recordingService;

    @Test
    void 녹화_토글_요청을_받으면_RecordingService에_위임한다() throws Exception {
        mockMvc.perform(patch("/api/paths/{pathName}/recording", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecordingToggleRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recordingService).toggleRecording("test", true);
    }

    @Test
    void pathName_필터_없이_녹화_목록을_조회한다() throws Exception {
        given(recordingService.getSegments(isNull())).willReturn(List.of(
                new RecordingSegmentResponse(1L, "test", "/recordings/test/seg1.mp4", LocalDateTime.of(2026, 7, 14, 10, 0))
        ));

        mockMvc.perform(get("/api/recordings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].pathName").value("test"))
                .andExpect(jsonPath("$.data[0].segmentPath").value("/recordings/test/seg1.mp4"));
    }

    @Test
    void pathName_필터로_녹화_목록을_조회한다() throws Exception {
        given(recordingService.getSegments(eq("test"))).willReturn(List.of());

        mockMvc.perform(get("/api/recordings").param("pathName", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(recordingService).getSegments("test");
    }
}
