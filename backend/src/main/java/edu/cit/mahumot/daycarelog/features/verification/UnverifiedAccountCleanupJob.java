package edu.cit.mahumot.daycarelog.features.verification;

import edu.cit.mahumot.daycarelog.features.guardians.GuardianRepository;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class UnverifiedAccountCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(UnverifiedAccountCleanupJob.class);
    private static final int GRACE_PERIOD_DAYS = 7;

    private final UserRepository userRepository;
    private final GuardianRepository guardianRepository;
    private final VerificationTokenRepository tokenRepository;

    public UnverifiedAccountCleanupJob(UserRepository userRepository, GuardianRepository guardianRepository,
                                        VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.guardianRepository = guardianRepository;
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupUnverifiedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GRACE_PERIOD_DAYS);
        List<User> stale = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(cutoff);

        int deleted = 0;
        int skipped = 0;
        for (User user : stale) {
            if (!guardianRepository.findByUserId(user.getId()).isEmpty()) {
                skipped++;
                log.info("Skipping cleanup of unverified account {} - has linked guardian records", user.getEmail());
                continue;
            }
            tokenRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
            deleted++;
            log.info("Deleted unverified account {} (registered {}, never verified within {} days)",
                    user.getEmail(), user.getCreatedAt(), GRACE_PERIOD_DAYS);
        }

        if (deleted > 0 || skipped > 0) {
            log.info("Unverified account cleanup complete: {} deleted, {} skipped", deleted, skipped);
        }
    }
}
