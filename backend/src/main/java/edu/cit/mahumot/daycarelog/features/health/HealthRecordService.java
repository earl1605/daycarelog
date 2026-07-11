package edu.cit.mahumot.daycarelog.features.health;

import edu.cit.mahumot.daycarelog.features.children.Child;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final ChildRepository childRepository;

    public HealthRecordService(HealthRecordRepository healthRecordRepository, ChildRepository childRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.childRepository = childRepository;
    }

    public List<HealthRecord> findAll() {
        return healthRecordRepository.findByDeletedAtIsNullOrderByMeasurementDateDesc();
    }

    public List<HealthRecord> findByChild(Long childId) {
        return healthRecordRepository.findByChildIdAndDeletedAtIsNullOrderByMeasurementDateDesc(childId);
    }

    public List<HealthRecord> findByChildIds(List<Long> childIds) {
        if (childIds.isEmpty()) return List.of();
        return healthRecordRepository.findByChildIdInAndDeletedAtIsNullOrderByMeasurementDateDesc(childIds);
    }

    public HealthRecord create(HealthRecordRequest req, Long userId) {
        Child child = childRepository.findById(req.getChildId())
                .orElseThrow(() -> new RuntimeException("Child not found"));
        LocalDate asOf = req.getMeasurementDate() != null ? req.getMeasurementDate() : LocalDate.now();
        String nutritionalStatus = NutritionalStatusCalculator.classify(
                req.getWeightKg(), child.getDateOfBirth(), child.getSex(), asOf);

        HealthRecord record = HealthRecord.builder()
                .childId(req.getChildId())
                .measurementDate(req.getMeasurementDate())
                .weightKg(req.getWeightKg())
                .heightCm(req.getHeightCm())
                .nutritionalStatus(nutritionalStatus)
                .remarks(req.getRemarks())
                .recordedBy(userId)
                .build();
        return healthRecordRepository.save(record);
    }

    public void delete(Long id) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        if (record.getDeletedAt() != null) {
            throw new RuntimeException("Health record already deleted");
        }
        record.setDeletedAt(LocalDateTime.now());
        healthRecordRepository.save(record);
    }

    public List<HealthRecord> findTrashed() {
        return healthRecordRepository.findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    public void restore(Long id) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Health record not found"));
        record.setDeletedAt(null);
        healthRecordRepository.save(record);
    }

    public void permanentlyDelete(Long id) {
        if (!healthRecordRepository.existsById(id)) {
            throw new RuntimeException("Health record not found");
        }
        healthRecordRepository.deleteById(id);
    }
}
