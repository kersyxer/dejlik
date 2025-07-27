package com.project.controller;

import com.project.dto.LoginRequest;
import com.project.dto.LoginResponse;
import com.project.dto.RefreshTokenRequest;
import com.project.dto.UserDto;
import com.project.entity.RefreshToken;
import com.project.entity.User;
import com.project.security.JwtUtil;
import com.project.service.RefreshTokenRepository;
import com.project.service.UserException;
import com.project.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = userService.findByEmail(req.getEmail());
            boolean ok = userService.loginUser(req.getEmail(),  req.getPassword());
            if(!ok){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
            }
            String accessToken;
            String refreshToken;
            try {
                accessToken = jwtUtil.generateAccessToken(user.getId(), user.getName(), user.getRole());
                refreshToken = jwtUtil.generateRefreshToken(user.getId());
                LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(jwtUtil.getRefreshTokenValiditySeconds());
                LocalDateTime createdAt = LocalDateTime.now();
                RefreshToken newRefreshToken = new RefreshToken(
                        UUID.randomUUID(),  // Генерація нового UUID для токену
                        user,
                        refreshToken,
                        expiredAt,   // Переходимо від LocalDateTime
                        createdAt
                );
                refreshTokenRepository.save(newRefreshToken);
            } catch(JwtException | IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token generation failed: " + e.getMessage());
            }
            UserDto dto = new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
            int expired_in = jwtUtil.getAccessTokenValiditySeconds();
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, expired_in, dto));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/getAccessToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest req) {
        try {
            var existingTokenOpt = refreshTokenRepository.findByToken(req.getRefreshToken());
            if (existingTokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
            }
            RefreshToken existingToken = existingTokenOpt.get();
            UUID userId = existingToken.getUser().getId();
            if (existingToken.getExpiredAt().isBefore(LocalDateTime.now())) {
                refreshTokenRepository.delete(existingToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Expired refresh token");
            }
            try {
                var claims = jwtUtil.extractAllClaims(req.getRefreshToken());
                if (!UUID.fromString(claims.getSubject()).equals(userId)) {
                    refreshTokenRepository.delete(existingToken);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token (subject mismatch)");
                }
            } catch (JwtException e) {
                refreshTokenRepository.delete(existingToken);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token (JWT integrity issue)");
            }
            User user = userService.findById(userId);
            String newAccess =  jwtUtil.generateAccessToken(user.getId(), user.getName(), user.getRole());
            return ResponseEntity.ok(Map.of("accessToken", newAccess));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or other user-related issue");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest req) {
        try {
            var existingTokenOpt = refreshTokenRepository.findByToken(req.getRefreshToken());
            if (existingTokenOpt.isPresent()) {
                refreshTokenRepository.delete(existingTokenOpt.get());
                return ResponseEntity.ok("Successfully logged out");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired refresh token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during logout: " + e.getMessage());
        }
    }
}
