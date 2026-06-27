package com.daycarelog.service;

import com.daycarelog.dto.UpdateProfileRequest;
import com.daycarelog.model.User;
import com.daycarelog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateRole(Long id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public User updateProfile(Long id, UpdateProfileRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
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
        if ("admin".equals(target.getRole()) && userRepository.countByRole("admin") <= 1) {
            throw new RuntimeException("Cannot delete the only admin account");
        }
        userRepository.deleteById(targetId);
    }
}
