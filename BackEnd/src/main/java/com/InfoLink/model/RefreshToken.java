package com.InfoLink.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
@Entity
public class RefreshToken {
    @Id
    @Column(name="token",nullable = false)
    private String token;
    @Column(name="username", nullable=false)
    private String username;
    @Column(name="expiryDate", nullable=false)
    private Instant expiryDate;
    @Column(name="lastUsed", nullable=false)
    private Instant lastUsed;
    public RefreshToken(String token, String username, Instant expiryDate, Instant lastUsed) {
        this.token = token;
        this.username = username;
        this.expiryDate = expiryDate;
        this.lastUsed = lastUsed;
    }
    public RefreshToken() {
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public Instant getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }
    public Instant getLastUsed() {
        return lastUsed;
    }
    public void setLastUsed(Instant lastUsed) {
        this.lastUsed = lastUsed;
    }
}
