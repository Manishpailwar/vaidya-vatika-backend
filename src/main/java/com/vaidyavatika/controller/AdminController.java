package com.vaidyavatika.controller;

import com.vaidyavatika.model.Order;
import com.vaidyavatika.model.Product;
import com.vaidyavatika.security.JwtUtil;
import com.vaidyavatika.service.OrderService;
import com.vaidyavatika.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @Value("${app.admin.password}")
    private String adminPassword;

    // ── DASHBOARD STATS ───────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderService.getAllOrders();

        long totalRevenue = orders.stream()
                .filter(o -> !o.getStatus().equals("CANCELLED"))
                .mapToLong(o -> o.getTotalAmount().longValue())
                .sum();

        long pendingOrders = orders.stream()
                .filter(o -> o.getStatus().equals("PLACED") || o.getStatus().equals("PROCESSING"))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", products.size());
        stats.put("totalOrders", orders.size());
        stats.put("pendingOrders", pendingOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("lowStockProducts", productService.getLowStockProducts().size());

        return ResponseEntity.ok(stats);
    }

    // ── VERIFY ADMIN PASSWORD → returns JWT ──────────────
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAdmin(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        boolean valid = adminPassword.equals(password);

        Map<String, Object> response = new HashMap<>();
        response.put("success", valid);
        response.put("message", valid ? "Admin access granted" : "Invalid admin password");

        if (valid) {
            // Issue a JWT with ADMIN role
            response.put("token", jwtUtil.generateAdminToken());
        }

        return ResponseEntity.ok(response);
    }
}