package edu.cit.mahumot.daycarelog.common.config;

import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
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

    @Value("${app.super-admin-seed.email:}")
    private String superAdminEmail;

    public AdminSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedFirstAdmin();
        promoteSuperAdmin();
    }

    private void seedFirstAdmin() {
        String email = seedEmail == null ? "" : seedEmail.trim();
        String password = seedPassword == null ? "" : seedPassword.trim();

        long adminCount = userRepository.countByRole("admin");
        log.info("AdminSeedRunner: existing admin count = {}, seed email set = {}, seed password set = {}",
                adminCount, !email.isBlank(), !password.isBlank());
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

    private void promoteSuperAdmin() {
        String email = superAdminEmail == null ? "" : superAdminEmail.trim();
        if (userRepository.countByRole("super_admin") > 0) {
            return;
        }
        if (email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            user.setRole("super_admin");
            userRepository.save(user);
            log.info("Promoted {} to super_admin.", email);
        }, () -> log.warn("SUPER_ADMIN_EMAIL ({}) does not match any existing account — skipping promotion.", email));
    }
}
