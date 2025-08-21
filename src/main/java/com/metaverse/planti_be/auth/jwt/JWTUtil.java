package com.metaverse.planti_be.auth.jwt;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JWTUtil {

    private final SecretKey secretKey;
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HS256");
    }

    //JWT 토큰에서 사용자 이름(username)을 추출합니다.
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    //JWT 토큰이 유효한지 검증합니다.
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    //새로운 JWT 토큰을 생성합니다.
    public String createJwt(String name, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("username", name)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    //JWT 토큰에서 특정 Claim을 추출하는 범용 메소드입니다.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 토큰에서 모든 클레임 추출
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey) // 서명 키 설정
                    .build() // 파서 빌드
                    .parseClaimsJws(token) // 토큰 파싱
                    .getBody(); // 클레임 바디 반환
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("만료된 JWT 토큰입니다.", e);
        } catch (MalformedJwtException | SecurityException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 JWT 토큰입니다.", e);
        }
    }

    //토큰의 만료 여부를 확인합니다
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    //토큰에서 만료 시간을 추출합니다.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}
