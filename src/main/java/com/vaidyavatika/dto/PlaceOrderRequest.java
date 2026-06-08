package com.vaidyavatika.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String customerEmail;

    @NotBlank(message = "Phone is required")
    private String customerPhone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    private Double totalAmount;
    private String paymentMethod;

    @NotNull(message = "Order items are required")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private String productName;
        private String productImage;
        private Integer quantity;
        private Double unitPrice;
    }
}
