package com.vaidyavatika.service;

import com.vaidyavatika.dto.LoginRequest;
import com.vaidyavatika.dto.RegisterRequest;
import com.vaidyavatika.dto.UpdateProfileRequest;
import com.vaidyavatika.exception.ResourceNotFoundException;
import com.vaidyavatika.model.PendingRegistration;
import com.vaidyavatika.model.User;
import com.vaidyavatika.repository.PendingRegistrationRepository;
import com.vaidyavatika.repository.UserRepository;
import com.vaidyavatika.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    // ── REGISTER ─────────────────────────────────────────
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists. Please log in.");
        }

        if (pendingRepo.existsByEmail(request.getEmail())) {
            pendingRepo.deleteByEmail(request.getEmail());
            pendingRepo.flush();
            log.info("Refreshing pending registration for: {}", request.getEmail());
        }

        String token = UUID.randomUUID().toString();

        PendingRegistration pending = PendingRegistration.builder()
                .name(request.getName())
                .email(request.getEmail())
                .encodedPassword(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        pendingRepo.save(pending);
        log.info("Pending registration saved for: {}", request.getEmail());

        // Send email async — failure won't block registration
        // User can request resend if email doesn't arrive
        try {
            emailService.sendVerificationEmail(request.getEmail(), request.getName(), token);
        } catch (Exception e) {
            log.warn("Could not send verification email to {}: {}", request.getEmail(), e.getMessage());
        }
    }

    // ── VERIFY EMAIL ─────────────────────────────────────
    @Transactional
    public User verifyEmail(String token) {
        PendingRegistration pending = pendingRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException(
                        "This verification link is invalid or has expired. Please register again."));

        if (LocalDateTime.now().isAfter(pending.getExpiresAt())) {
            pendingRepo.delete(pending);
            throw new RuntimeException("This verification link has expired. Please register again.");
        }

        if (userRepository.existsByEmail(pending.getEmail())) {
            pendingRepo.delete(pending);
            throw new RuntimeException("An account with this email already exists. Please log in.");
        }

        User user = User.builder()
                .name(pending.getName())
                .email(pending.getEmail())
                .password(pending.getEncodedPassword())
                .phone(pending.getPhone())
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        pendingRepo.delete(pending);
        log.info("Email verified — user created: {}", saved.getEmail());
        return saved;
    }

    // ── RESEND VERIFICATION ──────────────────────────────
    @Transactional
    public void resendVerification(String email) {
        PendingRegistration pending = pendingRepo.findByEmail(email)
                .orElseThrow(() -> {
                    if (userRepository.existsByEmail(email)) {
                        return new RuntimeException("This email is already verified. Please log in.");
                    }
                    return new RuntimeException("No pending registration found. Please register again.");
                });

        pending.setToken(UUID.randomUUID().toString());
        pending.setExpiresAt(LocalDateTime.now().plusHours(24));
        pendingRepo.save(pending);
        emailService.sendVerificationEmail(pending.getEmail(), pending.getName(), pending.getToken());
        log.info("Resent verification email to: {}", email);
    }

    // ── FORGOT PASSWORD ───────────────────────────────────
    // Generates a reset token and sends email.
    // Always returns success even if email not found (security — don't reveal
    // whether an account exists for a given email).
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
            log.info("Password reset email sent to: {}", email);
        });
    }

    // ── RESET PASSWORD ────────────────────────────────────
    // Validates token and sets new password.
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException(
                        "This reset link is invalid or has already been used."));

        if (LocalDateTime.now().isAfter(user.getResetTokenExpiresAt())) {
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
        log.info("Password reset successfully for: {}", user.getEmail());
    }

    // ── LOGIN ─────────────────────────────────────────────
    public User login(LoginRequest request) {
        if (pendingRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Your account has been deactivated");
        }

        log.info("User logged in: {}", request.getEmail());
        return user;
    }

    // ── GET PROFILE ───────────────────────────────────────
    public User getUserById(Long id, String callerEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        verifyOwnership(user, callerEmail);
        return user;
    }

    // ── UPDATE PROFILE ────────────────────────────────────
    public User updateProfile(Long id, UpdateProfileRequest request, String callerEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        verifyOwnership(user, callerEmail);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setPincode(request.getPincode());

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        log.info("Updated profile for user id: {}", id);
        return userRepository.save(user);
    }

    // ── BUILD RESPONSE ────────────────────────────────────
    public Map<String, Object> buildUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("address", user.getAddress());
        response.put("city", user.getCity());
        response.put("pincode", user.getPincode());
        response.put("joinedAt", user.getJoinedAt());
        response.put("token", jwtUtil.generateToken(user.getEmail()));
        return response;
    }

    // ── PRIVATE HELPER ────────────────────────────────────
    private void verifyOwnership(User user, String callerEmail) {
        if (callerEmail == null || !user.getEmail().equalsIgnoreCase(callerEmail)) {
            throw new RuntimeException("Access denied: you can only view or edit your own profile.");
        }
    }
}