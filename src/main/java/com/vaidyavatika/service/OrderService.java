package com.vaidyavatika.service;

import com.vaidyavatika.dto.PlaceOrderRequest;
import com.vaidyavatika.exception.ResourceNotFoundException;
import com.vaidyavatika.model.Order;
import com.vaidyavatika.model.OrderItem;
import com.vaidyavatika.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    // Place a new order
    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        log.info("Placing order for: {}", request.getCustomerEmail());

        // Build order items from request
        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setProductImage(itemReq.getProductImage());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setTotalPrice(itemReq.getUnitPrice() * itemReq.getQuantity());
            return item;
        }).collect(Collectors.toList());

        // Build the order
        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .pincode(request.getPincode())
                .totalAmount(request.getTotalAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "COD")
                .status("PLACED")
                .build();

        // Link each item back to the order
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        // Reduce stock for each product
        request.getItems().forEach(itemReq -> {
            if (itemReq.getProductId() != null) {
                try {
                    productService.reduceStock(itemReq.getProductId(), itemReq.getQuantity());
                } catch (Exception e) {
                    log.warn("Could not reduce stock for product {}: {}", itemReq.getProductId(), e.getMessage());
                }
            }
        });

        log.info("Order placed successfully with id: {}", savedOrder.getId());
        return savedOrder;
    }

    // Get all orders (admin)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get orders by customer email
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    // Get single order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    // Cancel order (customer)
    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrderById(id);
        if (!order.getStatus().equals("PLACED")) {
            throw new RuntimeException("Order can only be cancelled when status is PLACED. Current status: " + order.getStatus());
        }
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        log.info("Order {} cancelled", id);
        return orderRepository.save(order);
    }

    // Update order status (admin)
    @Transactional
    public Order updateStatus(Long id, String status) {
        List<String> validStatuses = List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(status)) {
            throw new RuntimeException("Invalid status: " + status + ". Valid values: " + validStatuses);
        }
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        log.info("Order {} status updated to {}", id, status);
        return orderRepository.save(order);
    }

    // Get orders by status (admin)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }
}
