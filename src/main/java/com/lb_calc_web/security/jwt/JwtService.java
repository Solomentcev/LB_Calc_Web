package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    @Value("${jwt.secret.access}")
    private String jwtAccessSecret;
    @Value("${jwt.secret.refresh}")
    private String jwtRefreshSecret;
    @Value("${jwt.expiration.access}")
    private long accessExpiration;
    @Value("${jwt.expiration.refresh}")
    private long refreshExpiration;


    public String extractEmail(String token) {
        logger.debug("Extracting email from token {}", token);
        Claims claims=getAccessClaims(token);
        return claims.getSubject();
    }

    public Claims getAccessClaims(String token) {
        return getAllClaims(token,jwtAccessSecret);
    }
    public Claims getRefreshClaims(String token) {

        return getAllClaims(token,jwtRefreshSecret);
    }
    public String generateAccessToken(EmployeeDTO employeeDTO) {
        return Jwts.builder()
                .claim("id", employeeDTO.getId())
                .claim("role", employeeDTO.getRole())
                .claim("type","access")
                .setSubject(employeeDTO.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey(jwtAccessSecret), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateRefreshToken(EmployeeDTO employeeDTO) {
        return Jwts.builder()
                .setSubject(employeeDTO.getEmail())
                .claim("ver", UUID.randomUUID().toString())
                .claim("type","refresh")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(jwtRefreshSecret), SignatureAlgorithm.HS256)
                .compact();
    }


    private Claims getAllClaims(String token, String secretKey) {
        logger.debug("Extracting claims from token {}", token);
        try {
            return Jwts
                        .parserBuilder()
                        .setSigningKey(getSigningKey(secretKey))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
        } catch (ClaimJwtException e) {
            return e.getClaims();
        }
    }

    public boolean isExpired(Claims c) {
        return c.getExpiration().before(new Date());
    }

    public boolean isAccess(Claims c) {
        return "access".equals(c.get("type"));
    }

    public boolean isRefresh(Claims c) {
        return "refresh".equals(c.get("type"));
    }
    public boolean isValidatedAccessToken(@NonNull String accessToken) {
        logger.debug("Validating access token {}", accessToken);
        return isValidatedToken(accessToken, jwtAccessSecret);
    }

    public boolean isValidatedRefreshToken(@NonNull String refreshToken) {
        logger.debug("Validating refresh token {}", refreshToken);
        return isValidatedToken(refreshToken, jwtRefreshSecret);
    }

    private boolean isValidatedToken(@NonNull String token, @NonNull String secretKey) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token);
            logger.debug("Токен is valid {}", token);
            return true;
        } catch (Exception e) {
            logger.debug("Токен invalid {}", token);
            logger.error(e.getMessage());
            return false;
        }
    }
    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public Cookie generateAccessTokenCookie(String token) {
        Cookie cookie = new Cookie("jwtAccess", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) accessExpiration);

        return cookie;
    }
    public Cookie generateRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie("jwtRefresh", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) refreshExpiration);
        return cookie;
    }
}
