package com.github.weasd33.playground.record.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.weasd33.playground.record.domain.RecordingSegment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class RecordingSegmentRepositoryTest {

    private static final String SEGMENT_PATH = "test_2026-07-14_10-00-00-000000.mp4";

    @Autowired
    private RecordingSegmentRepository recordingSegmentRepository;

    @Test
    void segmentPath가_중복되면_저장에_실패한다() {
        recordingSegmentRepository.saveAndFlush(new RecordingSegment("test", SEGMENT_PATH));

        assertThatThrownBy(() ->
                recordingSegmentRepository.saveAndFlush(new RecordingSegment("test", SEGMENT_PATH))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existsBySegmentPath는_저장된_segmentPath에_대해_true를_반환한다() {
        recordingSegmentRepository.save(new RecordingSegment("test", SEGMENT_PATH));

        assertThat(recordingSegmentRepository.existsBySegmentPath(SEGMENT_PATH)).isTrue();
        assertThat(recordingSegmentRepository.existsBySegmentPath("other.mp4")).isFalse();
    }
}
