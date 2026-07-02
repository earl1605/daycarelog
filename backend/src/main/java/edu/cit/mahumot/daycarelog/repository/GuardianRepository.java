package edu.cit.mahumot.daycarelog.repository;

import edu.cit.mahumot.daycarelog.model.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    List<Guardian> findByChildId(Long childId);
}
