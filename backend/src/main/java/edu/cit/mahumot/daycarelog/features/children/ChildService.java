package edu.cit.mahumot.daycarelog.features.children;

import edu.cit.mahumot.daycarelog.features.activity.ActivityActions;
import edu.cit.mahumot.daycarelog.features.activity.ActivityEntityTypes;
import edu.cit.mahumot.daycarelog.features.activity.ActivityLogService;
import edu.cit.mahumot.daycarelog.features.attendance.AttendanceRepository;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianRepository;
import edu.cit.mahumot.daycarelog.features.health.HealthRecordRepository;
import edu.cit.mahumot.daycarelog.features.immunizations.ImmunizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChildService {

    private final ChildRepository childRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AttendanceRepository attendanceRepository;
    private final ImmunizationRepository immunizationRepository;
    private final GuardianRepository guardianRepository;
    private final ActivityLogService activityLogService;

    public ChildService(ChildRepository childRepository, HealthRecordRepository healthRecordRepository,
                         AttendanceRepository attendanceRepository, ImmunizationRepository immunizationRepository,
                         GuardianRepository guardianRepository, ActivityLogService activityLogService) {
        this.childRepository = childRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.attendanceRepository = attendanceRepository;
        this.immunizationRepository = immunizationRepository;
        this.guardianRepository = guardianRepository;
        this.activityLogService = activityLogService;
    }

    public List<Child> findAll() {
        return childRepository.findAllByOrderByLastNameAsc();
    }

    public Child findById(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found"));
    }

    public List<Child> findByIds(List<Long> ids) {
        return childRepository.findAllById(ids);
    }

    public Child create(ChildRequest req, Long userId) {
        String status = req.getEnrollmentStatus() != null ? req.getEnrollmentStatus() : "active";
        if ("active".equals(status) && childRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirthAndEnrollmentStatus(
                req.getFirstName(), req.getLastName(), req.getDateOfBirth(), "active")) {
            throw new RuntimeException(req.getFirstName() + " " + req.getLastName()
                    + " is already actively enrolled with the same date of birth.");
        }
        Child child = Child.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .dateOfBirth(req.getDateOfBirth())
                .sex(req.getSex())
                .address(req.getAddress())
                .enrollmentDate(req.getEnrollmentDate())
                .enrollmentStatus(status)
                .allergies(req.getAllergies())
                .medicalConditions(req.getMedicalConditions())
                .bloodType(req.getBloodType())
                .createdBy(userId)
                .build();
        child = childRepository.save(child);
        activityLogService.log(userId, ActivityActions.CHILD_CREATED, ActivityEntityTypes.CHILD, child.getId(),
                child.getId(), "Registered child " + child.getFirstName() + " " + child.getLastName());
        return child;
    }

    public Child update(Long id, ChildRequest req, Long userId) {
        Child child = findById(id);
        String status = req.getEnrollmentStatus() != null ? req.getEnrollmentStatus() : child.getEnrollmentStatus();
        if ("active".equals(status) && childRepository.existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirthAndEnrollmentStatusAndIdNot(
                req.getFirstName(), req.getLastName(), req.getDateOfBirth(), "active", id)) {
            throw new RuntimeException(req.getFirstName() + " " + req.getLastName()
                    + " is already actively enrolled with the same date of birth.");
        }
        child.setFirstName(req.getFirstName());
        child.setLastName(req.getLastName());
        child.setDateOfBirth(req.getDateOfBirth());
        child.setSex(req.getSex());
        child.setAddress(req.getAddress());
        child.setEnrollmentDate(req.getEnrollmentDate());
        if (req.getEnrollmentStatus() != null) child.setEnrollmentStatus(req.getEnrollmentStatus());
        child.setAllergies(req.getAllergies());
        child.setMedicalConditions(req.getMedicalConditions());
        child.setBloodType(req.getBloodType());
        child = childRepository.save(child);
        activityLogService.log(userId, ActivityActions.CHILD_UPDATED, ActivityEntityTypes.CHILD, child.getId(),
                child.getId(), "Updated child " + child.getFirstName() + " " + child.getLastName());
        return child;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Child child = findById(id);
        String name = child.getFirstName() + " " + child.getLastName();
        healthRecordRepository.deleteByChildId(id);
        attendanceRepository.deleteByChildId(id);
        immunizationRepository.deleteByChildId(id);
        guardianRepository.deleteByChildId(id);
        childRepository.deleteById(id);
        // childId intentionally null here (not id) - the child row is gone, and the
        // FK is ON DELETE SET NULL anyway; entityId still records which id it was.
        activityLogService.log(userId, ActivityActions.CHILD_DELETED, ActivityEntityTypes.CHILD, id,
                null, "Removed child " + name);
    }
}
