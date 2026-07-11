package edu.cit.mahumot.daycarelog.features.health;

import edu.cit.mahumot.daycarelog.features.children.Child;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        return healthRecordRepository.findAllByOrderByMeasurementDateDesc();
    }

    public List<HealthRecord> findByChild(Long childId) {
        return healthRecordRepository.findByChildIdOrderByMeasurementDateDesc(childId);
    }

    public List<HealthRecord> findByChildIds(List<Long> childIds) {
        if (childIds.isEmpty()) return List.of();
        return healthRecordRepository.findByChildIdInOrderByMeasurementDateDesc(childIds);
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
        if (!healthRecordRepository.existsById(id)) {
            throw new RuntimeException("Health record not found");
        }
        healthRecordRepository.deleteById(id);
    }
}
