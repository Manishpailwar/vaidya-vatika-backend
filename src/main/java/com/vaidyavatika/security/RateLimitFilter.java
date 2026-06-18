package com.vaidyavatika.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for sensitive endpoints.
 *
 * Rules (per IP address, rolling 1-minute window):
 *   POST /api/v1/users/login    → max 10 attempts / minute
 *   POST /api/v1/users/register → max 5  attempts / minute
 *   POST /api/v1/admin/verify   → max 5  attempts / minute
 *
 * Why per-IP: credential stuffing and brute-force attacks come from
 * one (or a small number of) IPs hitting these endpoints in a loop.
 * A 10-per-minute cap makes a brute-force attack take years instead
 * of seconds, while legitimate users (who almost never hit the cap)
 * feel zero impact.
 *
 * Note: for multi-instance deployments, replace this with Redis-backed
 * rate limiting (e.g. Bucket4j + Redis). For a single-server setup
 * (which this app is right now) ConcurrentHashMap is perfectly fine.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    // How long a window lasts (milliseconds)
    private static final long WINDOW_MS = 60_000L;

    // Endpoint → max requests per window
    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/v1/users/login",    10,
            "/api/v1/users/register",  5,
            "/api/v1/admin/verify",    5
    );

    // ip:endpoint → bucket
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Integer limit = LIMITS.get(path);

        // Not a rate-limited endpoint — pass through immediately
        if (limit == null || !"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip  = getClientIp(request);
        String key = ip + ":" + path;

        Bucket bucket = buckets.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new Bucket(limit);
            }
            return existing;
        });

        if (bucket.tryConsume()) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP {} on {}", ip, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\"," +
                            "\"message\":\"Too many attempts. Please wait a minute and try again.\"}"
            );
        }
    }

    // ── Helpers ──────────────────────────────────────────

    /**
     * Extracts the real client IP, respecting common reverse-proxy headers.
     * Falls back to getRemoteAddr() if no proxy header is present.
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For can be a comma-separated list; first entry is the client
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    // ── Inner class: fixed-window bucket ─────────────────

    private static class Bucket {
        private final int maxRequests;
        private final AtomicInteger count;
        private final long windowStart;

        Bucket(int maxRequests) {
            this.maxRequests = maxRequests;
            this.count       = new AtomicInteger(0);
            this.windowStart = Instant.now().toEpochMilli();
        }

        /** Returns true if the window has expired and this bucket should be replaced. */
        boolean isExpired() {
            return Instant.now().toEpochMilli() - windowStart > WINDOW_MS;
        }

        /** Returns true if the request is within the limit, false if it should be blocked. */
        boolean tryConsume() {
            return count.incrementAndGet() <= maxRequests;
        }
    }
}