package com.vaidyavatika.controller;

import com.vaidyavatika.dto.LoginRequest;
import com.vaidyavatika.dto.RegisterRequest;
import com.vaidyavatika.dto.UpdateProfileRequest;
import com.vaidyavatika.model.User;
import com.vaidyavatika.service.UserService;
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
    // POST /api/v1/users/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.buildUserResponse(user));
    }

    // ── LOGIN ─────────────────────────────────────────────
    // POST /api/v1/users/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }

    // ── GET PROFILE ───────────────────────────────────────
    // GET /api/v1/users/1
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }

    // ── UPDATE PROFILE ────────────────────────────────────
    // PUT /api/v1/users/1
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(id, request);
        return ResponseEntity.ok(userService.buildUserResponse(user));
    }
}
