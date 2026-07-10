package edu.cit.mahumot.daycarelog.features.users;

import edu.cit.mahumot.daycarelog.common.util.TempPasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("super_admin", "admin", "staff");
    private static final Set<String> ADMIN_TIER_ROLES = Set.of("super_admin", "admin");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateRole(Long id, String role, String requesterRole) {
        if (role == null || !ALLOWED_ROLES.contains(role.toLowerCase().trim())) {
            throw new RuntimeException("Role must be 'super_admin', 'admin', or 'staff'");
        }
        String normalizedRole = role.toLowerCase().trim();
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if ("super_admin".equals(user.getRole())) {
            throw new RuntimeException("Super administrator accounts cannot be reassigned");
        }
        if (("admin".equals(user.getRole()) || ADMIN_TIER_ROLES.contains(normalizedRole)) && !"super_admin".equals(requesterRole)) {
            throw new RuntimeException("Only a super administrator can promote or demote admin accounts");
        }
        user.setRole(normalizedRole);
        return userRepository.save(user);
    }

    public CreatedUser createUser(CreateUserRequest req, String requesterRole) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        String role = req.getRole() == null ? "staff" : req.getRole().toLowerCase().trim();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new RuntimeException("Role must be 'super_admin', 'admin', or 'staff'");
        }
        if (ADMIN_TIER_ROLES.contains(role) && !"super_admin".equals(requesterRole)) {
            throw new RuntimeException("Only a super administrator can create admin accounts");
        }
        String tempPassword = TempPasswordGenerator.generate();
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .middleName(req.getMiddleName())
                .suffix(req.getSuffix())
                .role(role)
                .build();
        user = userRepository.save(user);
        return new CreatedUser(user, tempPassword);
    }

    public User deactivateUser(Long targetId, Long requesterId) {
        if (targetId.equals(requesterId)) throw new RuntimeException("You cannot deactivate your own account");
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        if ("super_admin".equals(target.getRole())) {
            throw new RuntimeException("Super administrator accounts cannot be deactivated");
        }
        if ("admin".equals(target.getRole()) && userRepository.countByRoleAndIsActiveTrue("admin") <= 1) {
            throw new RuntimeException("Cannot deactivate the only active admin account");
        }
        target.setIsActive(false);
        return userRepository.save(target);
    }

    public User reactivateUser(Long targetId) {
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        target.setIsActive(true);
        return userRepository.save(target);
    }

    public String resetPassword(Long targetId) {
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        String tempPassword = TempPasswordGenerator.generate();
        target.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(target);
        return tempPassword;
    }

    public record CreatedUser(User user, String tempPassword) {}

    public User updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (req.getEmail() != null && !req.getEmail().isBlank() && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getFirstName()   != null) user.setFirstName(req.getFirstName());
        if (req.getLastName()    != null) user.setLastName(req.getLastName());
        if (req.getMiddleName()  != null) user.setMiddleName(req.getMiddleName());
        if (req.getSuffix()      != null) user.setSuffix(req.getSuffix());
        if (req.getProfilePhoto() != null) user.setProfilePhoto(req.getProfilePhoto());
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long targetId, Long requesterId) {
        if (targetId.equals(requesterId)) throw new RuntimeException("You cannot delete your own account");
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        if ("super_admin".equals(target.getRole())) {
            throw new RuntimeException("Super administrator accounts cannot be deleted");
        }
        if ("admin".equals(target.getRole()) && userRepository.countByRole("admin") <= 1) {
            throw new RuntimeException("Cannot delete the only admin account");
        }
        userRepository.deleteById(targetId);
    }
}
