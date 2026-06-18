package com.vaidyavatika.controller;

import com.vaidyavatika.dto.LoginRequest;
import com.vaidyavatika.dto.RegisterRequest;
import com.vaidyavatika.dto.UpdateProfileRequest;
import com.vaidyavatika.model.User;
import com.vaidyavatika.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── REGISTER ──────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Please check your email (" + request.getEmail() + ") and click the verification link to activate your account.",
                "email", request.getEmail()
        ));
    }

    // ── LOGIN ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }

    // ── VERIFY EMAIL ──────────────────────────────────────
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        User user = userService.verifyEmail(token);
        Map<String, Object> response = userService.buildUserResponse(user);
        response.put("message", "Email verified! Welcome to Vaidya Vatika 🌿");
        return ResponseEntity.ok(response);
    }

    // ── RESEND VERIFICATION ───────────────────────────────
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        userService.resendVerification(email);
        return ResponseEntity.ok(Map.of("message", "Verification email resent! Please check your inbox."));
    }

    // ── FORGOT PASSWORD ───────────────────────────────────
    // POST /api/v1/users/forgot-password  { "email": "..." }
    // Always returns 200 even if email not found — security best practice.
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        userService.forgotPassword(email);
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists with this email, you will receive a password reset link shortly."
        ));
    }

    // ── RESET PASSWORD ────────────────────────────────────
    // POST /api/v1/users/reset-password  { "token": "...", "newPassword": "..." }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
        String token       = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Reset token is required"));
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }

        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully! You can now log in."));
    }

    // ── GET PROFILE ───────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfile(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        User user = userService.getUserById(id, callerEmail);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }

    // ── UPDATE PROFILE ────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        User user = userService.updateProfile(id, request, callerEmail);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }
}