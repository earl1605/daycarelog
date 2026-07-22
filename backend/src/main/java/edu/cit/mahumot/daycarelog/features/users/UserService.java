package edu.cit.mahumot.daycarelog.features.users;

import edu.cit.mahumot.daycarelog.common.util.TempPasswordGenerator;
import edu.cit.mahumot.daycarelog.features.activity.ActivityActions;
import edu.cit.mahumot.daycarelog.features.activity.ActivityEntityTypes;
import edu.cit.mahumot.daycarelog.features.activity.ActivityLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("admin", "staff");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateRole(Long id, String role, Long requesterId) {
        if (role == null || !ALLOWED_ROLES.contains(role.toLowerCase().trim())) {
            throw new RuntimeException("Role must be 'admin' or 'staff'");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        String oldRole = user.getRole();
        String newRole = role.toLowerCase().trim();
        user.setRole(newRole);
        user = userRepository.save(user);
        activityLogService.log(requesterId, ActivityActions.USER_ROLE_CHANGED, ActivityEntityTypes.USER, user.getId(),
                null, "Changed " + user.getFullName() + "'s role from " + oldRole + " to " + newRole);
        return user;
    }

    public CreatedUser createUser(CreateUserRequest req, Long requesterId) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        String role = req.getRole() == null ? "staff" : req.getRole().toLowerCase().trim();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new RuntimeException("Role must be 'admin' or 'staff'");
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
        activityLogService.log(requesterId, ActivityActions.USER_CREATED, ActivityEntityTypes.USER, user.getId(),
                null, "Created " + role + " account for " + user.getFullName());
        return new CreatedUser(user, tempPassword);
    }

    public User deactivateUser(Long targetId, Long requesterId) {
        if (targetId.equals(requesterId)) throw new RuntimeException("You cannot deactivate your own account");
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        if ("admin".equals(target.getRole()) && userRepository.countByRoleAndIsActiveTrue("admin") <= 1) {
            throw new RuntimeException("Cannot deactivate the only active admin account");
        }
        target.setIsActive(false);
        target = userRepository.save(target);
        activityLogService.log(requesterId, ActivityActions.USER_DEACTIVATED, ActivityEntityTypes.USER, target.getId(),
                null, "Deactivated " + target.getFullName() + "'s account");
        return target;
    }

    public User reactivateUser(Long targetId, Long requesterId) {
        User target = userRepository.findById(targetId).orElseThrow(() -> new RuntimeException("User not found"));
        target.setIsActive(true);
        target = userRepository.save(target);
        activityLogService.log(requesterId, ActivityActions.USER_REACTIVATED, ActivityEntityTypes.USER, target.getId(),
                null, "Reactivated " + target.getFullName() + "'s account");
        return target;
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
            if (req.getNewPassword().length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters");
            }
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
        if ("admin".equals(target.getRole()) && userRepository.countByRole("admin") <= 1) {
            throw new RuntimeException("Cannot delete the only admin account");
        }
        String name = target.getFullName();
        userRepository.deleteById(targetId);
        activityLogService.log(requesterId, ActivityActions.USER_DELETED, ActivityEntityTypes.USER, targetId,
                null, "Deleted account for " + name);
    }
}
