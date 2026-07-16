package com.github.weasd33.playground.record.scheduler;

import com.github.weasd33.playground.record.config.RecordingStorageProperties;
import com.github.weasd33.playground.record.domain.RecordingSegment;
import com.github.weasd33.playground.record.repository.RecordingSegmentRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordingReconciliationScheduler {

    private final RecordingSegmentRepository recordingSegmentRepository;
    private final RecordingStorageProperties recordingStorageProperties;

    @Scheduled(fixedDelay = 30_000)
    public void reconcile() {
        File[] files = new File(recordingStorageProperties.errorDir()).listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            reconcileSegment(file);
        }
    }

    private void reconcileSegment(File file) {
        String segmentPath = file.getName();
        if (!recordingSegmentRepository.existsBySegmentPath(segmentPath) && !trySave(segmentPath)) {
            return;
        }
        moveToCompleted(file);
    }

    private boolean trySave(String segmentPath) {
        try {
            recordingSegmentRepository.save(new RecordingSegment(extractPathName(segmentPath), segmentPath));
            return true;
        } catch (Exception e) {
            log.warn("[RecordingReconciliationScheduler] 세그먼트 DB 저장 실패, 다음 주기에 재시도: {}", segmentPath, e);
            return false;
        }
    }

    // 파일명 규칙이 "{pathName}_{timestamp}"(mediamtx.yml의 recordPath 패턴)라 pathName에
    // 언더스코어가 없다는 전제로 첫 언더스코어 앞부분을 추출한다.
    private String extractPathName(String segmentPath) {
        int underscoreIndex = segmentPath.indexOf('_');
        return underscoreIndex == -1 ? segmentPath : segmentPath.substring(0, underscoreIndex);
    }

    private void moveToCompleted(File file) {
        try {
            Path completedDir = Path.of(recordingStorageProperties.completedDir());
            Files.createDirectories(completedDir);
            Files.move(file.toPath(), completedDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("[RecordingReconciliationScheduler] completed/ 이동 실패: {}", file.getName(), e);
        }
    }
}
