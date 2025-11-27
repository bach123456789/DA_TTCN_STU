package com.web.DA_TTCN_STU.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private static final String SECRET = "supersecretkey123456supersecretkey123456"; // 32+ bytes
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 giờ

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Tạo token có roles nếu cần
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setClaims(Map.of("role", role))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Lấy role từ token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Parse toàn bộ claims trong token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn");
        } catch (UnsupportedJwtException e) {
            System.out.println("Token không hỗ trợ");
        } catch (MalformedJwtException e) {
            System.out.println("Token sai định dạng");
        } catch (SecurityException e) {
            System.out.println("Key không hợp lệ");
        } catch (IllegalArgumentException e) {
            System.out.println("Token rỗng");
        }
        return false;
    }
}
