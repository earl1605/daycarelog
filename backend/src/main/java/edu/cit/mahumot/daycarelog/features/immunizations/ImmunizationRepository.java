package edu.cit.mahumot.daycarelog.features.immunizations;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImmunizationRepository extends JpaRepository<Immunization, Long> {
    List<Immunization> findByDeletedAtIsNullOrderByDateGivenDesc();
    List<Immunization> findByChildIdAndDeletedAtIsNullOrderByDateGivenDesc(Long childId);
    List<Immunization> findByChildIdInAndDeletedAtIsNullOrderByDateGivenDesc(List<Long> childIds);
    boolean existsByChildIdAndVaccineNameAndDoseNumberAndDeletedAtIsNull(Long childId, String vaccineName, Integer doseNumber);
    List<Immunization> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
