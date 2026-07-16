package com.github.weasd33.playground.record.repository;

import com.github.weasd33.playground.record.domain.RecordingSegment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingSegmentRepository extends JpaRepository<RecordingSegment, Long> {

    List<RecordingSegment> findByPathName(String pathName);

    boolean existsBySegmentPath(String segmentPath);
}
