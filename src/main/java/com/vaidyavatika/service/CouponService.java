package com.vaidyavatika.service;

import com.vaidyavatika.model.Coupon;
import com.vaidyavatika.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;

    // ── VALIDATE COUPON ───────────────────────────────────
    // Returns discount amount. Throws if invalid.
    public Map<String, Object> validateCoupon(String code, double orderAmount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Invalid coupon code"));

        if (!coupon.getIsActive()) {
            throw new RuntimeException("This coupon is no longer active");
        }

        if (coupon.getExpiresAt() != null && LocalDateTime.now().isAfter(coupon.getExpiresAt())) {
            throw new RuntimeException("This coupon has expired");
        }

        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new RuntimeException("This coupon has reached its usage limit");
        }

        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            throw new RuntimeException(
                    "Minimum order amount of ₹" + coupon.getMinOrderAmount().intValue() + " required for this coupon"
            );
        }

        // Calculate discount
        double discount;
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            discount = orderAmount * coupon.getDiscountValue() / 100;
            if (coupon.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscountAmount());
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        discount = Math.min(discount, orderAmount); // can't discount more than order amount
        discount = Math.round(discount);

        log.info("Coupon {} validated — discount: ₹{}", code, discount);

        return Map.of(
                "code", coupon.getCode(),
                "discountType", coupon.getDiscountType(),
                "discountValue", coupon.getDiscountValue(),
                "discountAmount", discount,
                "message", coupon.getDiscountType().equals("PERCENTAGE")
                        ? (double) coupon.getDiscountValue() + "% off applied! You save ₹" + (int) discount
                        : "₹" + (int) discount + " off applied!"
        );
    }

    // ── APPLY COUPON (mark as used) ───────────────────────
    @Transactional
    public void applyCoupon(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }

    // ── RESTORE COUPON (decrement on order cancellation) ─
    @Transactional
    public void restoreCoupon(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(coupon -> {
            int restored = Math.max(0, coupon.getUsedCount() - 1); // never go below 0
            coupon.setUsedCount(restored);
            couponRepository.save(coupon);
            log.info("Coupon {} usage count restored to {}", code, restored);
        });
    }

    // ── ADMIN: CREATE COUPON ──────────────────────────────
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCodeIgnoreCase(coupon.getCode())) {
            throw new RuntimeException("Coupon code already exists");
        }
        coupon.setCode(coupon.getCode().toUpperCase());
        coupon.setUsedCount(0);
        return couponRepository.save(coupon);
    }

    // ── ADMIN: GET ALL COUPONS ────────────────────────────
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    // ── ADMIN: TOGGLE ACTIVE ──────────────────────────────
    @Transactional
    public Coupon toggleActive(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setIsActive(!coupon.getIsActive());
        return couponRepository.save(coupon);
    }

    // ── ADMIN: DELETE COUPON ──────────────────────────────
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}