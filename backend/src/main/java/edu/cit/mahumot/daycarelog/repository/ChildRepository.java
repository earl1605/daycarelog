package edu.cit.mahumot.daycarelog.repository;

import edu.cit.mahumot.daycarelog.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findAllByOrderByLastNameAsc();
    List<Child> findByEnrollmentStatus(String status);
}
