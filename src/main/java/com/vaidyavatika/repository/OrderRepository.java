package com.vaidyavatika.repository;

import com.vaidyavatika.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders by customer email
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String email);

    // Get all orders by status
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Get all orders newest first
    List<Order> findAllByOrderByCreatedAtDesc();
}
