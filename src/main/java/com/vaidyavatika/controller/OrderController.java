package com.vaidyavatika.controller;

import com.vaidyavatika.dto.PlaceOrderRequest;
import com.vaidyavatika.model.Order;
import com.vaidyavatika.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── PLACE ORDER ───────────────────────────────────────
    // Requires a valid user JWT. customerEmail is taken from the JWT itself —
    // the value in the request body is ignored — so a user cannot place an
    // order on behalf of someone else.
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpRequest) {

        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        if (callerEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Override whatever email the client sent — always use the JWT identity
        request.setCustomerEmail(callerEmail);

        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ── GET ALL ORDERS (Admin only) ───────────────────────
    // FIXED: @PreAuthorize ensures only tokens with ROLE_ADMIN can reach this.
    // A regular user JWT will get 403 Forbidden before the method even runs.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ── GET MY ORDERS ─────────────────────────────────────
    // Email extracted from verified JWT — not from user input.
    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(HttpServletRequest httpRequest) {
        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        return ResponseEntity.ok(orderService.getOrdersByEmail(callerEmail));
    }

    // ── GET SINGLE ORDER ──────────────────────────────────
    // Admins can view any order. Regular users can only view their own.
    // isAdmin is determined from the JWT role set in SecurityContext by JwtFilter —
    // never from request body or params.
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(orderService.getOrderById(id, callerEmail, isAdmin));
    }

    // ── CANCEL ORDER (Customer) ───────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        String callerEmail = (String) httpRequest.getAttribute("callerEmail");
        return ResponseEntity.ok(orderService.cancelOrder(id, callerEmail));
    }

    // ── UPDATE ORDER STATUS (Admin only) ─────────────────
    // FIXED: same @PreAuthorize guard as GET /orders.
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new RuntimeException("Status is required in request body");
        }
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}