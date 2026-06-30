package com.vaidyavatika.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double price;

    // Cloudinary HTTPS URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Lean JSON array of { name, type, url, size }
    @Column(name = "media_files", columnDefinition = "TEXT")
    private String mediaFiles;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 100)
    private String category;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @Column(length = 100)
    private String badge;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // ── Rich product content tabs ─────────────────────────
    // All stored as TEXT — admin can write detailed descriptions.
    // Shown as accordion/tabs on the product detail page.

    // Full product details — ingredients, sourcing, certifications etc.
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    // Step by step usage instructions
    @Column(name = "how_to_use", columnDefinition = "TEXT")
    private String howToUse;

    // Key ingredients list — one per line or comma separated
    @Column(name = "key_ingredients", columnDefinition = "TEXT")
    private String keyIngredients;

    // Weight, dimensions, shelf life, storage etc.
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}