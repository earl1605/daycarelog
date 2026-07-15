package edu.cit.mahumot.daycarelog.features.children;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findAllByOrderByLastNameAsc();
    List<Child> findByEnrollmentStatus(String status);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirthAndEnrollmentStatus(
            String firstName, String lastName, LocalDate dateOfBirth, String enrollmentStatus);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirthAndEnrollmentStatusAndIdNot(
            String firstName, String lastName, LocalDate dateOfBirth, String enrollmentStatus, Long id);
}
