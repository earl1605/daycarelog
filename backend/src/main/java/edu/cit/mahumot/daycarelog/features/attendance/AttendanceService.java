package edu.cit.mahumot.daycarelog.features.attendance;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
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
        Attendance att = attendanceRepository.findByChildIdAndDate(req.getChildId(), req.getDate())
                .orElse(Attendance.builder()
                        .childId(req.getChildId())
                        .date(req.getDate())
                        .recordedBy(userId)
                        .build());
        att.setStatus(req.getStatus());
        att.setTimeIn(req.getTimeIn());
        att.setTimeOut(req.getTimeOut());
        att.setRecordedBy(userId);
        return attendanceRepository.save(att);
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
