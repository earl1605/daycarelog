package edu.cit.mahumot.daycarelog.features.health;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findAllByOrderByMeasurementDateDesc();
    List<HealthRecord> findByChildIdOrderByMeasurementDateDesc(Long childId);
    List<HealthRecord> findByMeasurementDateBetween(LocalDate start, LocalDate end);
    List<HealthRecord> findByChildIdInOrderByMeasurementDateDesc(List<Long> childIds);
}
