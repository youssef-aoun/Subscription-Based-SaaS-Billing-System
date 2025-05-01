package com.youssef.Subscription_Based.SaaS.Billing.System.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResetPasswordRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(2)
                .refillGreedy(2, Duration.ofMinutes(30))
                .build();
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if ("/api/v1/users/reset-password".equals(request.getRequestURI())) {
            String ip = request.getRemoteAddr();
            Bucket bucket = ipBuckets.computeIfAbsent(ip, k -> createNewBucket());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429);
                response.getWriter().write("Too many password reset attempts. Please wait.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}