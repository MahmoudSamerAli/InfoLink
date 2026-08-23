package com.InfoLink.endPoints;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.InfoLink.dto.JwtResponse;
import com.InfoLink.dto.LoginRequest;
import com.InfoLink.dto.RefreshRequest;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.utils.JwtUtil;
import com.InfoLink.service.RefreshTokenService;
import com.InfoLink.model.RefreshToken;

@RestController
@RequestMapping("/auth")
public class AuthEP {
    private static final Logger logger = LoggerFactory.getLogger(AuthEP.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    public AuthEP(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                  RefreshTokenService refreshTokenService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();        // Generate short-lived access token
        String accessToken = jwtUtil.generateToken(userDetails);
        // Generate long-lived refresh token
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
        // Save refresh token in DB/Redis
        refreshTokenService.save(refreshToken, userDetails.getUsername());
        logger.info("User '{}' logged in successfully", userDetails.getUsername());
        return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest refreshReq) {
        String refreshToken = refreshReq.getRefreshToken();
        RefreshToken rt = refreshTokenService.validate(refreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(rt.getUsername());
        String newAccessToken = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(newAccessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody String refreshToken) {
        refreshTokenService.delete(refreshToken);
        logger.info("Refresh token invalidated, user logged out.");
        return ResponseEntity.ok().build();
    }
}
