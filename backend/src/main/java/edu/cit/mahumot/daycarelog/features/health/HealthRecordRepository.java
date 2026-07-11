package edu.cit.mahumot.daycarelog.features.health;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByDeletedAtIsNullOrderByMeasurementDateDesc();
    List<HealthRecord> findByChildIdAndDeletedAtIsNullOrderByMeasurementDateDesc(Long childId);
    List<HealthRecord> findByMeasurementDateBetweenAndDeletedAtIsNull(LocalDate start, LocalDate end);
    List<HealthRecord> findByChildIdInAndDeletedAtIsNullOrderByMeasurementDateDesc(List<Long> childIds);
    List<HealthRecord> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
