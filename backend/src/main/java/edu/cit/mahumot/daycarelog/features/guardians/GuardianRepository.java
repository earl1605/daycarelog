package edu.cit.mahumot.daycarelog.features.guardians;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    List<Guardian> findByChildId(Long childId);
    List<Guardian> findByUserId(Long userId);
    List<Guardian> findByUserIdIsNotNull();
    boolean existsByChildIdAndUserId(Long childId, Long userId);
    void deleteByUserId(Long userId);
    void deleteByChildId(Long childId);
}
