package edu.cit.mahumot.daycarelog.features.guardians;

import edu.cit.mahumot.daycarelog.common.email.EmailFormatValidator;
import edu.cit.mahumot.daycarelog.common.email.EmailRegistrationValidator;
import edu.cit.mahumot.daycarelog.features.activity.ActivityActions;
import edu.cit.mahumot.daycarelog.features.activity.ActivityEntityTypes;
import edu.cit.mahumot.daycarelog.features.activity.ActivityLogService;
import edu.cit.mahumot.daycarelog.features.guardians.GuardianAccountResponse.ChildSummary;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.common.util.TempPasswordGenerator;
import edu.cit.mahumot.daycarelog.features.verification.VerificationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final UserRepository userRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final EmailRegistrationValidator emailRegistrationValidator;
    private final EmailFormatValidator emailFormatValidator;
    private final ActivityLogService activityLogService;

    public GuardianService(GuardianRepository guardianRepository, UserRepository userRepository,
                            ChildRepository childRepository, PasswordEncoder passwordEncoder,
                            VerificationService verificationService,
                            EmailRegistrationValidator emailRegistrationValidator,
                            EmailFormatValidator emailFormatValidator,
                            ActivityLogService activityLogService) {
        this.guardianRepository = guardianRepository;
        this.userRepository = userRepository;
        this.childRepository = childRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.emailRegistrationValidator = emailRegistrationValidator;
        this.emailFormatValidator = emailFormatValidator;
        this.activityLogService = activityLogService;
    }

    public List<Guardian> findByChild(Long childId) {
        return guardianRepository.findByChildId(childId);
    }

    public List<Long> findChildIdsForUser(Long userId) {
        return guardianRepository.findByUserId(userId).stream().map(Guardian::getChildId).toList();
    }

    public boolean isGuardianOfChild(Long userId, Long childId) {
        return guardianRepository.existsByChildIdAndUserId(childId, userId);
    }

    public CreatedGuardian addGuardian(Long childId, GuardianRequest req, Long requesterId) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new RuntimeException("Guardian name is required");
        }
        Guardian guardian = new Guardian();
        guardian.setChildId(childId);
        guardian.setName(req.getName());
        guardian.setRelationship(req.getRelationship());
        guardian.setContactNumber(req.getContactNumber());
        guardian.setAddress(req.getAddress());
        guardian.setEmail(req.getEmail());
        guardian.setIsPrimary(req.getIsPrimary() != null && req.getIsPrimary());

        String tempPassword = null;
        if (req.getCreatePortalAccount() != null && req.getCreatePortalAccount()) {
            if (req.getEmail() == null || req.getEmail().isBlank()) {
                throw new RuntimeException("Email is required to create a parent portal account");
            }
            // Normalize (format-only) first so an existing account can be found by its
            // canonical email without re-running the disposable/MX checks against it --
            // those already passed when the account was first created, and re-checking
            // them here would wrongly block re-linking an existing guardian whose email
            // domain later stops resolving (or was a test/placeholder address).
            String normalized = emailFormatValidator.normalizeAndValidate(req.getEmail());
            Optional<User> existing = userRepository.findByEmail(normalized);
            User parentUser;
            String email;
            if (existing.isPresent()) {
                parentUser = existing.get();
                if (!"parent".equals(parentUser.getRole())) {
                    throw new RuntimeException("Email already belongs to an existing non-parent account");
                }
                email = parentUser.getEmail();
            } else {
                email = emailRegistrationValidator.validate(req.getEmail());
                tempPassword = TempPasswordGenerator.generate();
                parentUser = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(tempPassword))
                        .firstName(req.getName())
                        .role("parent")
                        .emailVerified(false)
                        .build();
                parentUser = userRepository.save(parentUser);
                verificationService.issueVerification(parentUser);
            }
            guardian.setEmail(email);
            guardian.setUserId(parentUser.getId());
        }

        guardian = guardianRepository.save(guardian);
        activityLogService.log(requesterId, ActivityActions.GUARDIAN_CREATED, ActivityEntityTypes.GUARDIAN,
                guardian.getId(), childId, "Added guardian " + guardian.getName());
        return new CreatedGuardian(guardian, tempPassword);
    }

    public void removeGuardian(Long childId, Long guardianId, Long requesterId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new RuntimeException("Guardian not found"));
        if (!guardian.getChildId().equals(childId)) {
            throw new RuntimeException("Guardian does not belong to this child");
        }
        guardianRepository.deleteById(guardianId);
        activityLogService.log(requesterId, ActivityActions.GUARDIAN_DELETED, ActivityEntityTypes.GUARDIAN,
                guardianId, childId, "Removed guardian " + guardian.getName());
    }

    public List<GuardianAccountResponse> findAllAccounts() {
        List<Guardian> rows = guardianRepository.findByUserIdIsNotNull();
        return rows.stream()
                .collect(Collectors.groupingBy(Guardian::getUserId))
                .entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<Guardian> forUser = entry.getValue();
                    Guardian first = forUser.get(0);
                    User user = userRepository.findById(userId).orElse(null);
                    List<ChildSummary> children = forUser.stream()
                            .map(Guardian::getChildId)
                            .map(childRepository::findById)
                            .flatMap(Optional::stream)
                            .map(c -> new ChildSummary(c.getId(), c.getFirstName(), c.getLastName()))
                            .toList();
                    return new GuardianAccountResponse(
                            userId, first.getName(),
                            user != null ? user.getEmail() : first.getEmail(),
                            first.getContactNumber(), first.getAddress(), first.getRelationship(),
                            children);
                })
                .sorted(Comparator.comparing(GuardianAccountResponse::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional
    public void removeAllForUser(Long userId) {
        guardianRepository.deleteByUserId(userId);
    }

    public record CreatedGuardian(Guardian guardian, String tempPassword) {}
}
