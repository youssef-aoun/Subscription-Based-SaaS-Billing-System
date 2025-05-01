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
public class CheckoutRateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> tokenBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket(){
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if("/api/v1/payments/checkout-session".equals(request.getRequestURI())){

            String token = extractJwtFromHeader(request);
            if (token == null) {
                response.setStatus(401);
                response.getWriter().write("Missing or invalid Authorization header");
                return;
            }

            Bucket bucket = tokenBuckets.computeIfAbsent(token, k -> createBucket());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if(probe.isConsumed()){
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe));
                filterChain.doFilter(request, response);
            }else {
                response.setStatus(429);
                response.getWriter().write("Too many checkout requests, please wait!");
            }
        }

        else{
            filterChain.doFilter(request, response);
        }
    }

    private String extractJwtFromHeader(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);
        return null;
    }
}
