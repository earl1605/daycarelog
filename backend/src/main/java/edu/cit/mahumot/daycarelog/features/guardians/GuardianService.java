package edu.cit.mahumot.daycarelog.features.guardians;

import edu.cit.mahumot.daycarelog.features.guardians.GuardianAccountResponse.ChildSummary;
import edu.cit.mahumot.daycarelog.features.users.User;
import edu.cit.mahumot.daycarelog.features.children.ChildRepository;
import edu.cit.mahumot.daycarelog.features.users.UserRepository;
import edu.cit.mahumot.daycarelog.common.util.TempPasswordGenerator;
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

    public GuardianService(GuardianRepository guardianRepository, UserRepository userRepository,
                            ChildRepository childRepository, PasswordEncoder passwordEncoder) {
        this.guardianRepository = guardianRepository;
        this.userRepository = userRepository;
        this.childRepository = childRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Guardian> findByChild(Long childId) {
        return guardianRepository.findByChildId(childId);
    }

    // Every child linked to this parent user, via their guardian rows.
    public List<Long> findChildIdsForUser(Long userId) {
        return guardianRepository.findByUserId(userId).stream().map(Guardian::getChildId).toList();
    }

    public boolean isGuardianOfChild(Long userId, Long childId) {
        return guardianRepository.existsByChildIdAndUserId(childId, userId);
    }

    public CreatedGuardian addGuardian(Long childId, GuardianRequest req) {
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
            Optional<User> existing = userRepository.findByEmail(req.getEmail());
            User parentUser;
            if (existing.isPresent()) {
                parentUser = existing.get();
                if (!"parent".equals(parentUser.getRole())) {
                    throw new RuntimeException("Email already belongs to an existing non-parent account");
                }
                // Reusing an existing parent account so the same login covers multiple children.
            } else {
                tempPassword = TempPasswordGenerator.generate();
                parentUser = User.builder()
                        .email(req.getEmail())
                        .password(passwordEncoder.encode(tempPassword))
                        .firstName(req.getName())
                        .role("parent")
                        .build();
                parentUser = userRepository.save(parentUser);
            }
            guardian.setUserId(parentUser.getId());
        }

        guardian = guardianRepository.save(guardian);
        return new CreatedGuardian(guardian, tempPassword);
    }

    public void removeGuardian(Long childId, Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new RuntimeException("Guardian not found"));
        if (!guardian.getChildId().equals(childId)) {
            throw new RuntimeException("Guardian does not belong to this child");
        }
        guardianRepository.deleteById(guardianId);
    }

    // One row per parent-portal-account, aggregating every child that account is
    // linked to. Contact-only guardians (no userId) don't have a login and are
    // managed from the child's own edit page instead, so they're excluded here.
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

    // Unlinks every child from this parent account (does not delete the User login
    // itself - that's a separate, more destructive action available on the Users page).
    @Transactional
    public void removeAllForUser(Long userId) {
        guardianRepository.deleteByUserId(userId);
    }

    public record CreatedGuardian(Guardian guardian, String tempPassword) {}
}
