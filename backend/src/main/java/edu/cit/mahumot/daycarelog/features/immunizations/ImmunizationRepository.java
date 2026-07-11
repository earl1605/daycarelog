package edu.cit.mahumot.daycarelog.features.immunizations;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImmunizationRepository extends JpaRepository<Immunization, Long> {
    List<Immunization> findAllByOrderByDateGivenDesc();
    List<Immunization> findByChildIdOrderByDateGivenDesc(Long childId);
    List<Immunization> findByChildIdInOrderByDateGivenDesc(List<Long> childIds);
}
