package com.vifinancenews.utilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SecretKey;
import java.util.Date;

public class JWTUtility {
    private static final SecretKey key = Jwts.SIG.HS256.key().build(); // Secure key generation

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    // Generate JWT Token
    public static String generateToken(String email) {
        return Jwts.builder()
                .header().add("alg", "HS256").and() // Define header with algorithm
                .subject(email) // Set subject (user email)
                .issuedAt(new Date()) // Token issue time
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expiry time
                .signWith(key) // Sign token with secret key
                .compact();
    }

    // Validate JWT Token
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key) // Verify signature
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false; // Token invalid or expired
        }
    }

    // Extract Email from Token
    public static String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
