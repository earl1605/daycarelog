package com.daycarelog.service;

import com.daycarelog.dto.HealthRecordRequest;
import com.daycarelog.model.HealthRecord;
import com.daycarelog.repository.HealthRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;

    public HealthRecordService(HealthRecordRepository healthRecordRepository) {
        this.healthRecordRepository = healthRecordRepository;
    }

    public List<HealthRecord> findAll() {
        return healthRecordRepository.findAllByOrderByMeasurementDateDesc();
    }

    public List<HealthRecord> findByChild(Long childId) {
        return healthRecordRepository.findByChildIdOrderByMeasurementDateDesc(childId);
    }

    public HealthRecord create(HealthRecordRequest req, Long userId) {
        HealthRecord record = HealthRecord.builder()
                .childId(req.getChildId())
                .measurementDate(req.getMeasurementDate())
                .weightKg(req.getWeightKg())
                .heightCm(req.getHeightCm())
                .nutritionalStatus(req.getNutritionalStatus())
                .remarks(req.getRemarks())
                .recordedBy(userId)
                .build();
        return healthRecordRepository.save(record);
    }
}
