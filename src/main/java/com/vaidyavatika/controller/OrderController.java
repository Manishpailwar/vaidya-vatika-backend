package com.vaidyavatika.controller;

import com.vaidyavatika.dto.PlaceOrderRequest;
import com.vaidyavatika.model.Order;
import com.vaidyavatika.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── PLACE ORDER ───────────────────────────────────────
    // POST /api/v1/orders
    @PostMapping
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        Order order = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ── GET ALL ORDERS (Admin) ────────────────────────────
    // GET /api/v1/orders
    // GET /api/v1/orders?status=PLACED
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // ── GET ORDERS BY CUSTOMER EMAIL ──────────────────────
    // GET /api/v1/orders/my?email=rahul@gmail.com
    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(@RequestParam String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }

    // ── GET SINGLE ORDER ──────────────────────────────────
    // GET /api/v1/orders/5
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ── CANCEL ORDER (Customer) ───────────────────────────
    // PUT /api/v1/orders/5/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // ── UPDATE ORDER STATUS (Admin) ───────────────────────
    // PUT /api/v1/orders/5/status
    // Body: { "status": "SHIPPED" }
    @PutMapping("/{id}/status")
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
