package com.InfoLink.utils;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.InfoLink.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${JWT_SECRET}")
    private String base64EncodedSecretKey;

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(CustomUserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("token_type", "access")
            .claim("roles", userDetails.getAuthorities())
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES))) // 15Min
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setId(UUID.randomUUID().toString())
            .claim("token_type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plus(8, ChronoUnit.HOURS)))
            .signWith(SignatureAlgorithm.HS256, getSigningKey())
            .compact();
    }
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) &&
               !isTokenExpired(token);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getClaims(token).get("token_type", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("token_type", String.class));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
