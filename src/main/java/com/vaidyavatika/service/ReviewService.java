package com.vaidyavatika.service;

import com.vaidyavatika.model.Review;
import com.vaidyavatika.repository.ReviewRepository;
import com.vaidyavatika.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    // ── GET REVIEWS FOR PRODUCT ───────────────────────────
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    // ── GET RATING SUMMARY ────────────────────────────────
    public Map<String, Object> getRatingSummary(Long productId) {
        Double avg   = reviewRepository.getAverageRatingByProductId(productId);
        Long   count = reviewRepository.getReviewCountByProductId(productId);
        return Map.of(
                "averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                "totalReviews",  count != null ? count : 0L
        );
    }

    // ── ADD REVIEW ────────────────────────────────────────
    @Transactional
    public Review addReview(Long productId, String userEmail, String userName,
                            int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        if (reviewRepository.existsByProductIdAndUserEmail(productId, userEmail)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        // Check if user has bought this product (verified purchase)
        boolean isVerified = orderRepository.findAll().stream()
                .anyMatch(order ->
                        order.getCustomerEmail().equalsIgnoreCase(userEmail) &&
                                order.getStatus().equals("DELIVERED") &&
                                order.getItems().stream()
                                        .anyMatch(item -> item.getProductId() != null &&
                                                item.getProductId().equals(productId))
                );

        Review review = Review.builder()
                .productId(productId)
                .userEmail(userEmail)
                .userName(userName)
                .rating(rating)
                .comment(comment)
                .isVerifiedPurchase(isVerified)
                .build();

        log.info("Review added for product {} by {}", productId, userEmail);
        return reviewRepository.save(review);
    }

    // ── DELETE REVIEW (Admin) ─────────────────────────────
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}