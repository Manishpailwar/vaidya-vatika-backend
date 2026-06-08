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

    // Primary image URL (used as fallback / thumbnail)
    private String imageUrl;

    // JSON string: array of { name, type, dataUrl } from the frontend uploader
    private String mediaFiles;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String badge;
}