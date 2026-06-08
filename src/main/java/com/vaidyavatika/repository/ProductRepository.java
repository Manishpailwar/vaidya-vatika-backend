package com.vaidyavatika.repository;

import com.vaidyavatika.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Get all active products
    List<Product> findByIsActiveTrue();

    // Filter by category
    List<Product> findByCategoryAndIsActiveTrue(String category);

    // Search by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    // Search by name or description
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(String query);

    // Find low stock products (for admin alerts)
    List<Product> findByStockLessThanAndIsActiveTrue(Integer threshold);
}
