package edu.cit.mahumot.daycarelog.features.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenHashAndTypeAndConsumedAtIsNull(String tokenHash, String type);

    Optional<VerificationToken> findFirstByUserIdAndTypeAndConsumedAtIsNullOrderByCreatedAtDesc(
            Long userId, String type);

    List<VerificationToken> findByUserIdAndConsumedAtIsNull(Long userId);

    long countByUserIdAndTypeAndCreatedAtAfter(Long userId, String type, LocalDateTime after);

    void deleteByUserId(Long userId);
}
