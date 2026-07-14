package com.github.weasd33.playground.record.service;

import com.github.weasd33.playground.media.client.MediaMtxApiClient;
import com.github.weasd33.playground.media.dto.MediaMtxRecordingWebhookRequest;
import com.github.weasd33.playground.record.domain.RecordingSegment;
import com.github.weasd33.playground.record.dto.RecordingSegmentResponse;
import com.github.weasd33.playground.record.repository.RecordingSegmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordingService {

    private final MediaMtxApiClient mediaMtxApiClient;
    private final RecordingSegmentRepository recordingSegmentRepository;

    public void toggleRecording(String pathName, boolean enabled) {
        mediaMtxApiClient.setRecording(pathName, enabled);
    }

    @Transactional
    public void saveSegment(MediaMtxRecordingWebhookRequest request) {
        recordingSegmentRepository.save(new RecordingSegment(request.pathName(), request.segmentPath()));
    }

    public List<RecordingSegmentResponse> getSegments(String pathName) {
        List<RecordingSegment> segments = StringUtils.hasText(pathName)
                ? recordingSegmentRepository.findByPathName(pathName)
                : recordingSegmentRepository.findAll();
        return segments.stream()
                .map(RecordingSegmentResponse::from)
                .toList();
    }
}
