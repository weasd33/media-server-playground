package com.github.weasd33.playground.record.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.weasd33.playground.record.config.RecordingStorageProperties;
import com.github.weasd33.playground.record.domain.RecordingSegment;
import com.github.weasd33.playground.record.repository.RecordingSegmentRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordingReconciliationSchedulerTest {

    private static final String SEGMENT_FILE_NAME = "test_2026-07-14_10-00-00-000000.mp4";

    @Mock
    private RecordingSegmentRepository recordingSegmentRepository;

    @TempDir
    private Path recordingDir;

    @TempDir
    private Path errorDir;

    @TempDir
    private Path completedDir;

    private RecordingReconciliationScheduler scheduler;

    @BeforeEach
    void setUp() {
        RecordingStorageProperties properties = new RecordingStorageProperties(recordingDir.toString(), errorDir.toString(), completedDir.toString());
        scheduler = new RecordingReconciliationScheduler(recordingSegmentRepository, properties);
    }

    @Test
    void DB에_미존재하는_세그먼트는_저장_후_completed로_이동한다() throws IOException {
        Path segment = createErrorFile(SEGMENT_FILE_NAME);
        given(recordingSegmentRepository.existsBySegmentPath(SEGMENT_FILE_NAME)).willReturn(false);
        given(recordingSegmentRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        scheduler.reconcile();

        ArgumentCaptor<RecordingSegment> captor = ArgumentCaptor.forClass(RecordingSegment.class);
        verify(recordingSegmentRepository).save(captor.capture());
        assertThat(captor.getValue().getPathName()).isEqualTo("test");
        assertThat(captor.getValue().getSegmentPath()).isEqualTo(SEGMENT_FILE_NAME);
        assertThat(Files.exists(segment)).isFalse();
        assertThat(Files.exists(completedDir.resolve(SEGMENT_FILE_NAME))).isTrue();
    }

    @Test
    void DB에_이미_존재하는_세그먼트는_저장_없이_completed로만_이동한다() throws IOException {
        createErrorFile(SEGMENT_FILE_NAME);
        given(recordingSegmentRepository.existsBySegmentPath(SEGMENT_FILE_NAME)).willReturn(true);

        scheduler.reconcile();

        verify(recordingSegmentRepository, never()).save(any());
        assertThat(Files.exists(completedDir.resolve(SEGMENT_FILE_NAME))).isTrue();
    }

    @Test
    void DB_저장에_실패하면_파일을_error에_그대로_둔다() throws IOException {
        Path segment = createErrorFile(SEGMENT_FILE_NAME);
        given(recordingSegmentRepository.existsBySegmentPath(SEGMENT_FILE_NAME)).willReturn(false);
        given(recordingSegmentRepository.save(any())).willThrow(new RuntimeException("DB down"));

        scheduler.reconcile();

        assertThat(Files.exists(segment)).isTrue();
        assertThat(Files.exists(completedDir.resolve(SEGMENT_FILE_NAME))).isFalse();
    }

    private Path createErrorFile(String fileName) throws IOException {
        Path file = errorDir.resolve(fileName);
        Files.writeString(file, "dummy");
        return file;
    }
}
