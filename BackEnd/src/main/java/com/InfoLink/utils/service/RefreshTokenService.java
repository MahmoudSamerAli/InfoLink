package com.InfoLink.utils.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InfoLink.model.RefreshToken;
import com.InfoLink.repository.RefreshTokenRepository;
import com.InfoLink.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final JwtUtil jwtUtil;
    public RefreshTokenService(RefreshTokenRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }
    @Transactional
    public synchronized String rotate(String token) {
        Claims claims = parseRefreshToken(token);
        String tokenHash = hashToken(token);
        RefreshToken rt = repo.findById(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(rt);
            throw new BadCredentialsException("Refresh token expired");
        }
        if (rt.getLastUsed().isBefore(Instant.now().minus(30, ChronoUnit.MINUTES))) {
            repo.delete(rt);
            throw new BadCredentialsException("Session expired due to inactivity");
        }

        repo.delete(rt);
        String replacement = jwtUtil.generateRefreshToken(rt.getUsername());
        save(replacement, rt.getUsername());
        return replacement;
    }

    public void save(String token, String username) {
        Claims claims = parseRefreshToken(token);
        if (!username.equals(claims.getSubject())) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        RefreshToken rt = new RefreshToken();
        rt.setToken(hashToken(token));
        rt.setUsername(username);
        rt.setExpiryDate(claims.getExpiration().toInstant());
        rt.setLastUsed(Instant.now());
        repo.save(rt);
    }

    public void delete(String token) {
        if (token != null && !token.isBlank()) {
            repo.deleteById(hashToken(token));
        }
    }

    private Claims parseRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        try {
            if (!jwtUtil.isRefreshToken(token)) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            return jwtUtil.getClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid refresh token", ex);
        }
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
