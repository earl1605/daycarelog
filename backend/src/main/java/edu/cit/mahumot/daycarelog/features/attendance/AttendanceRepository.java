package edu.cit.mahumot.daycarelog.features.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByChildIdOrderByDateDesc(Long childId);
    Optional<Attendance> findByChildIdAndDate(Long childId, LocalDate date);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    List<Attendance> findByChildIdAndDateBetween(Long childId, LocalDate start, LocalDate end);
    List<Attendance> findByChildIdInOrderByDateDesc(List<Long> childIds);
}
