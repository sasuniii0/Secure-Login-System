package com.example.secure_login.config;

import com.example.secure_login.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String ip = getClientIp(request);
        Bucket bucket = null;

        if (path.equals("/api/auth/login")){
            bucket = rateLimiterService.getLoginBucket(ip);
        }else if (path.equals("/api/auth/verify-otp")){
            bucket = rateLimiterService.getOtpBucket(ip);
        }else if (path.equals("/api/auth/register")){
            bucket = rateLimiterService.getRegisterBucket(ip);
        }

        // If this endpoint is rate-limited, check the bucket
        if (bucket != null) {
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                // request allowed - add helpful headers so the clients knows their limits
                response.addHeader("X-RateLimit-Remaining",
                        String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            }else{
                // rate limit exceeded
                long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(
                        probe.getNanosToWaitForRefill());
                sendRateLimitError(response, waitSeconds);
            }
        } else {
            // not rate limit endpoint
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Sends a clean JSON error response instead of a raw 429 page.
     */
    private void sendRateLimitError(HttpServletResponse response, long waitSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.addHeader("Retry-After" , String.valueOf(waitSeconds));

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message",
                String.format("Too many requests. Please wait %d seconds before trying again.",
                        waitSeconds));
        body.put("retryAfterSeconds", waitSeconds);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /**
     * Extracts the real client IP.
     * Handles cases where your app is behind a reverse proxy (Nginx, load balancer).
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null &&  !forwarded.isBlank() ) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
