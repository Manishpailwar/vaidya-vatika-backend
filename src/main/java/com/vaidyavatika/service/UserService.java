package com.vaidyavatika.service;

import com.vaidyavatika.dto.LoginRequest;
import com.vaidyavatika.dto.RegisterRequest;
import com.vaidyavatika.dto.UpdateProfileRequest;
import com.vaidyavatika.exception.ResourceNotFoundException;
import com.vaidyavatika.model.User;
import com.vaidyavatika.repository.UserRepository;
import com.vaidyavatika.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists");
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .isActive(true)
                .build();
        log.info("Registered new user: {}", request.getEmail());
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
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

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateProfile(Long id, UpdateProfileRequest request) {
        User user = getUserById(id);
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
        // Include JWT token in login/register response
        response.put("token", jwtUtil.generateToken(user.getEmail()));
        return response;
    }
}