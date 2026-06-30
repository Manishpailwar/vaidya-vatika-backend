package com.vaidyavatika.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;          // e.g. SAVE10, WELCOME20

    // PERCENTAGE or FIXED
    @Column(nullable = false, length = 20)
    private String discountType;

    @Column(nullable = false)
    private Double discountValue;  // 10 = 10% or ₹10

    @Column(name = "min_order_amount")
    private Double minOrderAmount; // minimum cart value to apply

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount; // cap for percentage discounts

    @Column(name = "max_uses")
    private Integer maxUses;       // null = unlimited

    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}