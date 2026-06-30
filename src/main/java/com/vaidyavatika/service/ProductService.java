package com.vaidyavatika.service;

import com.vaidyavatika.dto.ProductRequest;
import com.vaidyavatika.exception.ResourceNotFoundException;
import com.vaidyavatika.model.Product;
import com.vaidyavatika.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product addProduct(ProductRequest request) {
        log.info("Adding new product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(resolveImageUrl(request.getImageUrl()))
                .mediaFiles(request.getMediaFiles())
                .category(request.getCategory())
                .stock(request.getStock())
                .badge(request.getBadge())
                .details(request.getDetails())
                .howToUse(request.getHowToUse())
                .keyIngredients(request.getKeyIngredients())
                .specifications(request.getSpecifications())
                .isActive(true)
                .build();
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(resolveImageUrl(request.getImageUrl()));
        product.setMediaFiles(request.getMediaFiles());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setBadge(request.getBadge());
        product.setDetails(request.getDetails());
        product.setHowToUse(request.getHowToUse());
        product.setKeyIngredients(request.getKeyIngredients());
        product.setSpecifications(request.getSpecifications());
        log.info("Updated product id: {}", id);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Soft-deleted product id: {}", id);
    }

    public void reduceStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Reduced stock for product {} by {}. Remaining: {}", productId, quantity, product.getStock());
    }

    public void restoreStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        log.info("Restored stock for product {} by {}. New total: {}", productId, quantity, product.getStock());
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockLessThanAndIsActiveTrue(10);
    }

    private String resolveImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank() && imageUrl.startsWith("http")) {
            return imageUrl;
        }
        return null;
    }
}