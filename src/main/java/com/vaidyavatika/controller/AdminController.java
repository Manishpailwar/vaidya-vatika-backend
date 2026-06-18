package com.vaidyavatika.controller;

import com.vaidyavatika.repository.OrderRepository;
import com.vaidyavatika.repository.ProductRepository;
import com.vaidyavatika.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final JwtUtil jwtUtil;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Value("${app.admin.password}")
    private String adminPassword;

    // ── ADMIN LOGIN ───────────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAdmin(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || !password.equals(adminPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid admin password"));
        }
        String token = jwtUtil.generateAdminToken();
        return ResponseEntity.ok(Map.of("valid", true, "token", token));
    }

    // ── VERIFY ADMIN TOKEN ────────────────────────────────
    @GetMapping("/verify-token")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> verifyToken() {
        return ResponseEntity.ok(Map.of("valid", true));
    }

    // ── ADMIN STATS ───────────────────────────────────────
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();
        long pendingOrders = orderRepository.findByStatusOrderByCreatedAtDesc("PLACED").size()
                + orderRepository.findByStatusOrderByCreatedAtDesc("PROCESSING").size();
        long lowStockCount = productRepository.findByStockLessThanAndIsActiveTrue(10).size();

        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0)
                .sum();

        return ResponseEntity.ok(Map.of(
                "totalProducts",  totalProducts,
                "totalOrders",    totalOrders,
                "pendingOrders",  pendingOrders,
                "lowStockProducts", lowStockCount,
                "totalRevenue",   Math.round(totalRevenue)
        ));
    }
}