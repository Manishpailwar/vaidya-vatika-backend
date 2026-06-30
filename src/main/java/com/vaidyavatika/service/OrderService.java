package com.vaidyavatika.service;

import com.vaidyavatika.dto.PlaceOrderRequest;
import com.vaidyavatika.exception.ResourceNotFoundException;
import com.vaidyavatika.model.Order;
import com.vaidyavatika.model.Product;
import com.vaidyavatika.model.OrderItem;
import com.vaidyavatika.repository.OrderRepository;
import com.vaidyavatika.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final EmailService emailService;
    private final CouponService couponService;

    // ── PLACE ORDER ───────────────────────────────────────
    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for: {}", request.getCustomerEmail());

        // Step 1 — Reduce stock first (inside transaction)
        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() != null) {
                productService.reduceStock(itemReq.getProductId(), itemReq.getQuantity());
            }
        }

        // Step 2 — Build order items using REAL prices from the database
        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            Product product = productService.getProductById(itemReq.getProductId());
            double realPrice = product.getPrice();
            int    qty       = itemReq.getQuantity();

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(itemReq.getProductImage());
            item.setQuantity(qty);
            item.setUnitPrice(realPrice);
            item.setTotalPrice(realPrice * qty);
            return item;
        }).collect(Collectors.toList());

        // Step 3 — Recalculate grand total server-side
        double subtotal   = items.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        double shipping   = subtotal > 499 ? 0 : 49;
        double gst        = Math.round(subtotal * 0.05);
        double beforeDiscount = subtotal + shipping + gst;

        // Step 4 — Apply coupon discount if provided (validated server-side)
        double discount = 0;
        String appliedCouponCode = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            try {
                Map<String, Object> couponResult = couponService.validateCoupon(
                        request.getCouponCode(), subtotal);
                discount = ((Number) couponResult.get("discountAmount")).doubleValue();
                appliedCouponCode = request.getCouponCode().toUpperCase();
                couponService.applyCoupon(appliedCouponCode);
                log.info("Coupon {} applied — discount: ₹{}", appliedCouponCode, discount);
            } catch (Exception e) {
                log.warn("Coupon {} rejected during order placement: {}", request.getCouponCode(), e.getMessage());
                throw new RuntimeException("Coupon error: " + e.getMessage());
            }
        }

        double grandTotal = Math.max(0, beforeDiscount - discount);

        log.info("Order total: subtotal={} shipping={} gst={} discount={} total={}", subtotal, shipping, gst, discount, grandTotal);

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .pincode(request.getPincode())
                .totalAmount(grandTotal)
                .discountAmount(discount)
                .couponCode(appliedCouponCode)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .status("PLACED")
                .build();

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with id: {}", savedOrder.getId());

        // Send confirmation email async — does not affect order placement
        emailService.sendOrderConfirmationEmail(savedOrder);

        return savedOrder;
    }

    // ── GET ALL ORDERS (Admin) ────────────────────────────
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    // ── GET SINGLE ORDER ──────────────────────────────────
    public Order getOrderById(Long id, String callerEmail, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (!isAdmin) {
            if (callerEmail == null || !order.getCustomerEmail().equalsIgnoreCase(callerEmail)) {
                throw new ResourceNotFoundException("Order not found with id: " + id);
            }
        }

        return order;
    }

    // ── CANCEL ORDER (Customer) ───────────────────────────
    @Transactional
    public Order cancelOrder(Long id, String callerEmail) {
        Order order = getOrderById(id, callerEmail, false);

        if (!order.getStatus().equals("PLACED")) {
            throw new RuntimeException(
                    "Order can only be cancelled when status is PLACED. Current status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        order.getItems().forEach(item -> {
            if (item.getProductId() != null) {
                try {
                    productService.restoreStock(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.warn("Could not restore stock for product {}: {}", item.getProductId(), e.getMessage());
                }
            }
        });

        // Restore coupon usage count if one was applied to this order
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            try {
                couponService.restoreCoupon(order.getCouponCode());
            } catch (Exception e) {
                log.warn("Could not restore coupon usage for {}: {}", order.getCouponCode(), e.getMessage());
            }
        }

        log.info("Order {} cancelled by {}", id, callerEmail);
        return orderRepository.save(order);
    }

    // ── UPDATE ORDER STATUS (Admin) ───────────────────────
    @Transactional
    public Order updateStatus(Long id, String status) {
        List<String> validStatuses = List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid status: " + status + ". Valid values: " + validStatuses);
        }
        Order order = getOrderById(id, null, true);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        log.info("Order {} status updated to {} by admin", id, status);
        Order saved = orderRepository.save(order);

        // Send status update email for SHIPPED and DELIVERED only
        emailService.sendOrderStatusEmail(saved);

        return saved;
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }
}