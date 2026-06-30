package com.vaidyavatika.controller;

import com.vaidyavatika.model.Review;
import com.vaidyavatika.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ── GET REVIEWS FOR PRODUCT ───────────────────────────
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    // ── GET RATING SUMMARY ────────────────────────────────
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }

    // ── ADD REVIEW (logged in users only) ─────────────────
    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> addReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String callerEmail = (String) request.getAttribute("callerEmail");
        String callerName  = (String) request.getAttribute("callerName");
        int    rating      = ((Number) body.get("rating")).intValue();
        String comment     = (String) body.getOrDefault("comment", "");
        return ResponseEntity.ok(
                reviewService.addReview(productId, callerEmail, callerName, rating, comment)
        );
    }

    // ── DELETE REVIEW (Admin) ─────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }
}