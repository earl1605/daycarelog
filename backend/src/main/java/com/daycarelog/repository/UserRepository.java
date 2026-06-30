package com.daycarelog.repository;

import com.daycarelog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(String role);
    long countByRoleAndIsActiveTrue(String role);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = 'staff' WHERE u.role = 'teacher'")
    int migrateTeacherRoleToStaff();
}
