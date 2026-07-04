package edu.cit.mahumot.daycarelog.config;

import edu.cit.mahumot.daycarelog.model.User;
import edu.cit.mahumot.daycarelog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-seed.email:}")
    private String seedEmail;

    @Value("${app.admin-seed.password:}")
    private String seedPassword;

    public AdminSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Runs once per boot: only creates the seed admin when no admin-role
    // account exists yet, so it's safe to leave ADMIN_SEED_EMAIL/PASSWORD set
    // indefinitely after the first admin has been created.
    @Override
    public void run(String... args) {
        String email = seedEmail == null ? "" : seedEmail.trim();
        String password = seedPassword == null ? "" : seedPassword.trim();

        long adminCount = userRepository.countByRole("admin");
        log.info("AdminSeedRunner: existing admin count = {}, ADMIN_SEED_EMAIL set = {}", adminCount, !email.isBlank());
        if (adminCount > 0) {
            return;
        }
        if (email.isBlank() || password.isBlank()) {
            log.warn("No admin account exists and ADMIN_SEED_EMAIL/ADMIN_SEED_PASSWORD are not set — " +
                    "set both env vars and redeploy to create the first admin account.");
            return;
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("ADMIN_SEED_EMAIL ({}) already belongs to an existing non-admin account — skipping seed.", email);
            return;
        }
        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName("Admin")
                .lastName("")
                .role("admin")
                .build();
        userRepository.save(admin);
        log.info("Seeded initial admin account: {}", email);
    }
}
