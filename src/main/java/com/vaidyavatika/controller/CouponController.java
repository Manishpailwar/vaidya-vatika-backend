package com.vaidyavatika.controller;

import com.vaidyavatika.model.Coupon;
import com.vaidyavatika.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    // ── VALIDATE COUPON (user) ────────────────────────────
    // POST /api/v1/coupons/validate  { "code": "SAVE10", "orderAmount": 500 }
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, Object> body) {
        String code        = (String) body.get("code");
        double orderAmount = ((Number) body.get("orderAmount")).doubleValue();
        return ResponseEntity.ok(couponService.validateCoupon(code, orderAmount));
    }

    // ── ADMIN: GET ALL ────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // ── ADMIN: CREATE ─────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    // ── ADMIN: TOGGLE ACTIVE ──────────────────────────────
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Coupon> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.toggleActive(id));
    }

    // ── ADMIN: DELETE ─────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(Map.of("message", "Coupon deleted"));
    }
}