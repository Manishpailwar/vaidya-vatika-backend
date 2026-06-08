package com.vaidyavatika.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String address;
    private String city;
    private String pincode;
    private String currentPassword;
    private String newPassword;
}
