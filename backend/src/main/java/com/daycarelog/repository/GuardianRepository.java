package com.daycarelog.repository;

import com.daycarelog.model.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    List<Guardian> findByChildId(Long childId);
}
