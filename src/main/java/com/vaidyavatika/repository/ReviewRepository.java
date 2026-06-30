package com.vaidyavatika.repository;

import com.vaidyavatika.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    Optional<Review> findByProductIdAndUserEmail(Long productId, String userEmail);
    boolean existsByProductIdAndUserEmail(Long productId, String userEmail);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Long getReviewCountByProductId(Long productId);
}