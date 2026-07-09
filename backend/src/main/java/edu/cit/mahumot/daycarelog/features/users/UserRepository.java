package edu.cit.mahumot.daycarelog.features.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(String role);
    long countByRoleAndIsActiveTrue(String role);

    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoff);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = 'staff' WHERE u.role = 'teacher'")
    int migrateTeacherRoleToStaff();
}
