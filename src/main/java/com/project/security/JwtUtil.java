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
    private long accessTokenMinutes;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenMinutes;

    private Date now() {
        return new Date();
    }

    private Date expiration(int minutes) {
        return new Date(now().getTime() + minutes * 60_000);
    }

    public String generateAccessToken(Integer userID, String name, String role) {
        return Jwts.builder()
                .setSubject(userID.toString())
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(now())
                .setExpiration(expiration((int) accessTokenMinutes))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(Integer userID) {
        return Jwts.builder()
                .setSubject(userID.toString())
                .setIssuedAt(now())
                .setExpiration(expiration((int) refreshTokenMinutes))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token) throws ExpiredJwtException, JwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessTokenValiditySeconds() {
        return accessTokenMinutes * 60;
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(now());
    }
}
