package edu.cit.mahumot.daycarelog.service;

import edu.cit.mahumot.daycarelog.dto.GuardianRequest;
import edu.cit.mahumot.daycarelog.model.Guardian;
import edu.cit.mahumot.daycarelog.model.User;
import edu.cit.mahumot.daycarelog.repository.GuardianRepository;
import edu.cit.mahumot.daycarelog.repository.UserRepository;
import edu.cit.mahumot.daycarelog.util.TempPasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GuardianService(GuardianRepository guardianRepository, UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.guardianRepository = guardianRepository;
        this.userRepository = userRepository;
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

    public record CreatedGuardian(Guardian guardian, String tempPassword) {}
}
