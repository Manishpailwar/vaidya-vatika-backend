package com.vaidyavatika.controller;

import com.vaidyavatika.dto.ProductRequest;
import com.vaidyavatika.model.Product;
import com.vaidyavatika.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── GET ALL PRODUCTS ──────────────────────────────────
    // GET /api/v1/products
    // GET /api/v1/products?category=Honey
    // GET /api/v1/products?search=daliya
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        List<Product> products;

        if (search != null && !search.isBlank()) {
            products = productService.searchProducts(search);
        } else if (category != null && !category.isBlank()) {
            products = productService.getByCategory(category);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(products);
    }

    // ── GET SINGLE PRODUCT ────────────────────────────────
    // GET /api/v1/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── ADD PRODUCT (Admin) ───────────────────────────────
    // POST /api/v1/products
    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    // ── UPDATE PRODUCT (Admin) ────────────────────────────
    // PUT /api/v1/products/1
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ── DELETE PRODUCT (Admin) ────────────────────────────
    // DELETE /api/v1/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    // ── LOW STOCK ALERT (Admin) ───────────────────────────
    // GET /api/v1/products/low-stock
    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }
}
