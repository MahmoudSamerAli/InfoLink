package com.InfoLink.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentMap<String, RequestWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Integer limit = getLimit(request);
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + ":" + rateLimitScope(request);
        RequestWindow window = windows.computeIfAbsent(key, ignored -> new RequestWindow());

        if (!window.tryConsume(limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Integer getLimit(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return request.getRequestURI().startsWith("/api/") ? 100 : null;
        }
        return switch (request.getRequestURI()) {
            case "/auth/login" -> 5;
            case "/auth/refresh" -> 10;
            default -> request.getRequestURI().startsWith("/api/") ? 100 : null;
        };
    }

    private String rateLimitScope(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            return "api";
        }
        return request.getRequestURI();
    }

    private static final class RequestWindow {
        private long startedAt = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger();

        private synchronized boolean tryConsume(int limit) {
            long now = System.currentTimeMillis();
            if (now - startedAt >= WINDOW_MILLIS) {
                startedAt = now;
                count.set(0);
            }
            return count.incrementAndGet() <= limit;
        }
    }
}