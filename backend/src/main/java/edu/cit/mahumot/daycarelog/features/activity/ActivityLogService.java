package edu.cit.mahumot.daycarelog.features.activity;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Never throws. A failed audit write must not roll back or fail the
     * operation being logged, so any exception is caught and logged at WARN.
     */
    public void log(Long userId, String action, String entityType, Long entityId, Long childId, String description) {
        try {
            ActivityLog entry = ActivityLog.builder()
                    .userId(userId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .childId(childId)
                    .description(description)
                    .build();
            activityLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to write activity log [action={}, entityType={}, entityId={}]: {}",
                    action, entityType, entityId, e.getMessage());
        }
    }

    public Page<ActivityLogResponse> findRecent(Pageable pageable) {
        return toResponsePage(activityLogRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    public Page<ActivityLogResponse> findByChild(Long childId, Pageable pageable) {
        return toResponsePage(activityLogRepository.findByChildIdOrderByCreatedAtDesc(childId, pageable));
    }

    // Built as a Specification (type-safe Criteria API), not a hand-written JPQL
    // @Query - a "(:param IS NULL OR field = :param)" JPQL string for this exact
    // optional-multi-field-filter shape hit a PostgreSQL parameter-type-inference
    // issue in production (confirmed by isolating it: swapping this one call out
    // for a simpler query made the failure disappear). Specification generates
    // parameters from the actual Java types below, so there's no string for
    // Postgres to fail to infer a type for in the first place.
    public Page<ActivityLogResponse> search(String action, String entityType, Long userId,
                                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<ActivityLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null) predicates.add(cb.equal(root.get("action"), action));
            if (entityType != null) predicates.add(cb.equal(root.get("entityType"), entityType));
            if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return toResponsePage(activityLogRepository.findAll(spec, pageable));
    }

    /** Batch-resolves actor names for a page of logs in one query instead of one lookup per row. */
    private Page<ActivityLogResponse> toResponsePage(Page<ActivityLog> page) {
        List<Long> userIds = page.getContent().stream()
                .map(ActivityLog::getUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> namesByUserId = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));

        return page.map(a -> new ActivityLogResponse(
                a.getId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getChildId(),
                a.getDescription(),
                a.getUserId() == null ? "System" : namesByUserId.getOrDefault(a.getUserId(), "Deleted user"),
                a.getCreatedAt()));
    }
}
