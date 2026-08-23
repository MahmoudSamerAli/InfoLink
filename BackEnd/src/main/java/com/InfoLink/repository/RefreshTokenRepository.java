package com.InfoLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.InfoLink.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String>{}