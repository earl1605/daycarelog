package edu.cit.mahumot.daycarelog.features.activity;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Page<ActivityLogResponse> search(String action, String entityType, Long userId,
                                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return toResponsePage(activityLogRepository.search(action, entityType, userId, from, to, pageable));
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
