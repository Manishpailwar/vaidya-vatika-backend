package com.vaidyavatika.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private String imageUrl;
    private String mediaFiles;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String badge;

    // ── Rich content tabs ─────────────────────────────────
    private String details;
    private String howToUse;
    private String keyIngredients;
    private String specifications;
}