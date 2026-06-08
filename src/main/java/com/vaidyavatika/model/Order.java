package com.vaidyavatika.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email required")
    @Column(name = "customer_email", nullable = false, length = 200)
    private String customerEmail;

    @NotBlank(message = "Phone is required")
    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Pincode is required")
    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "payment_method", length = 50)
    @Builder.Default
    private String paymentMethod = "COD";

    // PLACED → PROCESSING → SHIPPED → DELIVERED → CANCELLED
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PLACED";

    // One order has many order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
