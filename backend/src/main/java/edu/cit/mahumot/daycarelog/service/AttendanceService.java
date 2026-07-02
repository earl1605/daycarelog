package edu.cit.mahumot.daycarelog.service;

import edu.cit.mahumot.daycarelog.dto.AttendanceRequest;
import edu.cit.mahumot.daycarelog.model.Attendance;
import edu.cit.mahumot.daycarelog.repository.AttendanceRepository;
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

    // Daycare only operates Monday-Friday; reject weekend dates at the write path.
    // Read endpoints (findByDate/findByChild/findByDateRange) are intentionally left
    // unrestricted so any pre-existing data remains viewable.
    private void validateWeekday(LocalDate date) {
        if (date == null) throw new RuntimeException("Date is required");
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Attendance cannot be recorded for a Saturday or Sunday (" + date + ")");
        }
    }
}
