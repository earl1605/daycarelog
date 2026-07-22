package edu.cit.mahumot.daycarelog.features.attendance;

import edu.cit.mahumot.daycarelog.features.activity.ActivityActions;
import edu.cit.mahumot.daycarelog.features.activity.ActivityEntityTypes;
import edu.cit.mahumot.daycarelog.features.activity.ActivityLogService;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ChildRepository childRepository;
    private final ActivityLogService activityLogService;

    public AttendanceService(AttendanceRepository attendanceRepository, ChildRepository childRepository,
                              ActivityLogService activityLogService) {
        this.attendanceRepository = attendanceRepository;
        this.childRepository = childRepository;
        this.activityLogService = activityLogService;
    }

    public List<Attendance> findByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public List<Attendance> findByChild(Long childId) {
        return attendanceRepository.findByChildIdOrderByDateDesc(childId);
    }

    public List<Attendance> findByDateRange(LocalDate start, LocalDate end) {
        return attendanceRepository.findByDateBetween(start, end);
    }

    public List<Attendance> findByChildIds(List<Long> childIds) {
        if (childIds.isEmpty()) return List.of();
        return attendanceRepository.findByChildIdInOrderByDateDesc(childIds);
    }

    public Attendance upsert(AttendanceRequest req, Long userId) {
        validateWeekday(req.getDate());
        var existing = attendanceRepository.findByChildIdAndDate(req.getChildId(), req.getDate());
        // The web Attendance page's "Save Attendance" always re-submits every child's
        // status in one bulk call, whether or not it actually changed. Logging every
        // upsert() unconditionally would flood the activity feed with a "Marked ...
        // Present" entry for every child on every save, including ones nobody touched.
        // Only log when it's a genuinely new record or the status actually changed.
        boolean statusChanged = existing.isEmpty() || !existing.get().getStatus().equals(req.getStatus());

        Attendance att = existing.orElse(Attendance.builder()
                .childId(req.getChildId())
                .date(req.getDate())
                .recordedBy(userId)
                .build());
        att.setStatus(req.getStatus());
        att.setTimeIn(req.getTimeIn());
        att.setTimeOut(req.getTimeOut());
        att.setRecordedBy(userId);
        att = attendanceRepository.save(att);

        if (statusChanged) {
            String childName = childRepository.findById(req.getChildId())
                    .map(c -> c.getFirstName() + " " + c.getLastName())
                    .orElse("child #" + req.getChildId());
            activityLogService.log(userId, ActivityActions.ATTENDANCE_RECORDED, ActivityEntityTypes.ATTENDANCE, att.getId(),
                    req.getChildId(), "Marked " + childName + " " + req.getStatus() + " on " + req.getDate());
        }
        return att;
    }

    public List<Attendance> bulkUpsert(List<AttendanceRequest> requests, Long userId) {
        return requests.stream().map(r -> upsert(r, userId)).toList();
    }

    private void validateWeekday(LocalDate date) {
        if (date == null) throw new RuntimeException("Date is required");
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Attendance cannot be recorded for a Saturday or Sunday (" + date + ")");
        }
    }
}
