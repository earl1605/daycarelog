package edu.cit.mahumot.daycarelog.config;

import edu.cit.mahumot.daycarelog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleMigrationRunner.class);

    private final UserRepository userRepository;

    public RoleMigrationRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        int migrated = userRepository.migrateTeacherRoleToStaff();
        if (migrated > 0) {
            log.info("Role migration: moved {} account(s) from 'teacher' to 'staff'", migrated);
        }
    }
}
