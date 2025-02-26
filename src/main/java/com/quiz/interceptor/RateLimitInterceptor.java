package com.quiz.interceptor;

import com.quiz.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.MediaType;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Value("${rate.limit.duration}")
    private int duration;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = resolveKey(request);
        Bucket bucket = rateLimitConfig.resolveBucket(key);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(duration));
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimitConfig.getRateLimit()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(Math.max(0, bucket.getAvailableTokens())));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\n");
        response.getWriter().write("  \"error\": \"Rate limit exceeded. Try again in " + (duration / 1000) + " seconds.\"\n");
        response.getWriter().write("}\n");

        return false;
    }

    private String resolveKey(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return username + "-" + request.getRequestURI();
    }
} 