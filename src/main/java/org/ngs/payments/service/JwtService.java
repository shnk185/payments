package org.ngs.payments.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.ngs.payments.config.properties.JwtConfig;
import org.ngs.payments.entity.UserEntity;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiryMs;

    public JwtService(JwtConfig jwtConfig) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtConfig.getBase64Secret()));
        this.expiryMs = jwtConfig.getExpiryMs();
    }

    public String generate(UserEntity user) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(user.getId())).claim("role", user.getUserType().name())
                .issuedAt(now).expiration(new Date(now.getTime() + expiryMs)).signWith(secretKey).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
