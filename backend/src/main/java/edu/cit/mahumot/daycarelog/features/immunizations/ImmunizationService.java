package edu.cit.mahumot.daycarelog.features.immunizations;

import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImmunizationService {

    private final ImmunizationRepository immunizationRepository;
    private final ChildRepository childRepository;

    public ImmunizationService(ImmunizationRepository immunizationRepository, ChildRepository childRepository) {
        this.immunizationRepository = immunizationRepository;
        this.childRepository = childRepository;
    }

    public List<Immunization> findAll() {
        return immunizationRepository.findByDeletedAtIsNullOrderByDateGivenDesc();
    }

    public List<Immunization> findByChild(Long childId) {
        return immunizationRepository.findByChildIdAndDeletedAtIsNullOrderByDateGivenDesc(childId);
    }

    public List<Immunization> findByChildIds(List<Long> childIds) {
        if (childIds.isEmpty()) return List.of();
        return immunizationRepository.findByChildIdInAndDeletedAtIsNullOrderByDateGivenDesc(childIds);
    }

    public Immunization create(ImmunizationRequest req) {
        if (!childRepository.existsById(req.getChildId())) {
            throw new RuntimeException("Child not found");
        }
        if (!EpiVaccineSchedule.isKnownVaccine(req.getVaccineName())) {
            throw new RuntimeException("Unknown vaccine: " + req.getVaccineName());
        }
        Integer expectedDoses = EpiVaccineSchedule.expectedDoses(req.getVaccineName());
        if (req.getDoseNumber() == null || req.getDoseNumber() < 1 || req.getDoseNumber() > expectedDoses) {
            throw new RuntimeException(req.getVaccineName() + " only has " + expectedDoses + " expected dose(s)");
        }
        if (immunizationRepository.existsByChildIdAndVaccineNameAndDoseNumberAndDeletedAtIsNull(
                req.getChildId(), req.getVaccineName(), req.getDoseNumber())) {
            throw new RuntimeException(req.getVaccineName() + " dose " + req.getDoseNumber() + " is already recorded for this child");
        }

        Immunization record = Immunization.builder()
                .childId(req.getChildId())
                .vaccineName(req.getVaccineName())
                .doseNumber(req.getDoseNumber())
                .dateGiven(req.getDateGiven())
                .administeredBy(req.getAdministeredBy())
                .notes(req.getNotes())
                .build();
        return immunizationRepository.save(record);
    }

    public void delete(Long id) {
        Immunization record = immunizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Immunization record not found"));
        if (record.getDeletedAt() != null) {
            throw new RuntimeException("Immunization record already deleted");
        }
        record.setDeletedAt(LocalDateTime.now());
        immunizationRepository.save(record);
    }

    public List<Immunization> findTrashed() {
        return immunizationRepository.findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    public void restore(Long id) {
        Immunization record = immunizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Immunization record not found"));
        if (immunizationRepository.existsByChildIdAndVaccineNameAndDoseNumberAndDeletedAtIsNull(
                record.getChildId(), record.getVaccineName(), record.getDoseNumber())) {
            throw new RuntimeException("Cannot restore: " + record.getVaccineName() + " dose " + record.getDoseNumber() + " was already re-recorded for this child");
        }
        record.setDeletedAt(null);
        immunizationRepository.save(record);
    }

    public void permanentlyDelete(Long id) {
        if (!immunizationRepository.existsById(id)) {
            throw new RuntimeException("Immunization record not found");
        }
        immunizationRepository.deleteById(id);
    }
}
