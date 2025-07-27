package com.project.security;

import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private int accessTokenMinutes;

    @Value("${jwt.refresh.expiration}")
    private int refreshTokenMinutes;

    private Date now() {
        return new Date();
    }

    public Date expiration(int minutes) {
        return new Date(now().getTime() + (long) minutes * 60 * 1000);
    }

    public String generateAccessToken(UUID userID, String name, String role) {
        return Jwts.builder()
                .setSubject(userID.toString())
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(now())
                .setExpiration(expiration(accessTokenMinutes))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(UUID userID) {
        return Jwts.builder()
                .setSubject(userID.toString())
                .setIssuedAt(now())
                .setExpiration(expiration(refreshTokenMinutes))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token) throws ExpiredJwtException, JwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public int getAccessTokenValiditySeconds() {
        return accessTokenMinutes * 60;
    }

    public int getRefreshTokenValiditySeconds() {
        return refreshTokenMinutes;
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(now());
    }
}
