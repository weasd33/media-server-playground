package com.github.weasd33.playground.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.weasd33.playground.media.client.MediaMtxApiClient;
import com.github.weasd33.playground.media.dto.MediaMtxRecordingWebhookRequest;
import com.github.weasd33.playground.record.domain.RecordingSegment;
import com.github.weasd33.playground.record.dto.RecordingSegmentResponse;
import com.github.weasd33.playground.record.repository.RecordingSegmentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordingServiceTest {

    @Mock
    private MediaMtxApiClient mediaMtxApiClient;

    @Mock
    private RecordingSegmentRepository recordingSegmentRepository;

    private RecordingService recordingService;

    @BeforeEach
    void setUp() {
        recordingService = new RecordingService(mediaMtxApiClient, recordingSegmentRepository);
    }

    @Test
    void 녹화_토글_요청은_MediaMtxApiClient에_위임한다() {
        recordingService.toggleRecording("test", true);

        verify(mediaMtxApiClient).setRecording("test", true);
    }

    @Test
    void 웹훅_페이로드를_RecordingSegment로_변환해_저장한다() {
        MediaMtxRecordingWebhookRequest request = new MediaMtxRecordingWebhookRequest("test", "/recordings/test/seg1.mp4");
        ArgumentCaptor<RecordingSegment> captor = ArgumentCaptor.forClass(RecordingSegment.class);
        given(recordingSegmentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        recordingService.saveSegment(request);

        verify(recordingSegmentRepository).save(captor.capture());
        RecordingSegment saved = captor.getValue();
        assertThat(saved.getPathName()).isEqualTo("test");
        assertThat(saved.getSegmentPath()).isEqualTo("/recordings/test/seg1.mp4");
        assertThat(saved.getReceivedAt()).isNotNull();
    }

    @Test
    void pathName이_없으면_전체_세그먼트를_조회한다() {
        given(recordingSegmentRepository.findAll()).willReturn(List.of(new RecordingSegment("test", "/recordings/test/seg1.mp4")));

        List<RecordingSegmentResponse> result = recordingService.getSegments(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).pathName()).isEqualTo("test");
        verify(recordingSegmentRepository, never()).findByPathName(any());
    }

    @Test
    void pathName이_있으면_해당_경로로_필터링해_조회한다() {
        given(recordingSegmentRepository.findByPathName(eq("test"))).willReturn(List.of(new RecordingSegment("test", "/recordings/test/seg1.mp4")));

        List<RecordingSegmentResponse> result = recordingService.getSegments("test");

        assertThat(result).hasSize(1);
        verify(recordingSegmentRepository).findByPathName("test");
        verify(recordingSegmentRepository, never()).findAll();
    }
}
