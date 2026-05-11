package com.farmerassistant.repository;

import com.farmerassistant.entity.DiseaseLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DiseaseLog entity operations.
 */
@Repository
public interface DiseaseLogRepository extends JpaRepository<DiseaseLog, Long> {

    /**
     * Fetch paginated disease logs for a specific user.
     */
    Page<DiseaseLog> findByUserIdOrderByDetectedAtDesc(Long userId, Pageable pageable);

    /**
     * Count detections made by a specific user.
     */
    long countByUserId(Long userId);
}
