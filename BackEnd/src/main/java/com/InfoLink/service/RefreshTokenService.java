package com.InfoLink.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.InfoLink.model.RefreshToken;
import com.InfoLink.repository.RefreshTokenRepository;
import com.InfoLink.utils.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository repo;
    @Autowired
    private JwtUtil jwtUtil;
    public RefreshToken validate(String token) {
        RefreshToken rt = repo.findById(token)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        if (rt.getLastUsed().isBefore(Instant.now().minus(30, ChronoUnit.MINUTES))) {
            repo.delete(rt);
            throw new RuntimeException("Session expired due to inactivity");
        }

        rt.setLastUsed(Instant.now());
        repo.save(rt);
        return rt;
    }
    public void save(String token, String username) {
    Claims claims = jwtUtil.getClaims(token);
    Instant expiryDate = claims.getExpiration().toInstant();
    RefreshToken rt = new RefreshToken();
    rt.setToken(token);
    rt.setUsername(username);
    rt.setExpiryDate(expiryDate); // use exp from token
    rt.setLastUsed(Instant.now());
    repo.save(rt);
    }
    public void delete(String token) {
        repo.deleteById(token);
    }
}
