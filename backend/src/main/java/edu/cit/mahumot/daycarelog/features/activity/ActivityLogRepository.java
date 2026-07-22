package edu.cit.mahumot.daycarelog.features.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long>, JpaSpecificationExecutor<ActivityLog> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId, Pageable pageable);

    Page<ActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ActivityLog> findByChildIdOrderByCreatedAtDesc(Long childId, Pageable pageable);

    // search(...) in ActivityLogService builds a Specification for the optional
    // multi-field filter instead of a hand-written JPQL @Query - see the comment
    // there for why: a "(:param IS NULL OR field = :param)" JPQL pattern hit a
    // PostgreSQL parameter-type-inference issue in production that a type-safe
    // Criteria API query (what Specification generates) can't have.
}
