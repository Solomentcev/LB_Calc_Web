package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        logger.debug("[JWT][extractEmail] start token...");
        Claims claims = getAccessClaims(token);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new JwtException("Access token does not contain subject(email)");
        }
        logger.debug("[JWT][extractEmail] success subject={}", subject);
        return subject;
    }

    public Claims getAccessClaims(String token) {
        logger.debug("[JWT][getAccessClaims] start token...");
        Claims claims = parseClaimsStrict(token, jwtAccessSecret, "access");
        logger.debug("[JWT][getAccessClaims] success subject={}", claims.getSubject());
        return claims;
    }
    public Claims getRefreshClaims(String token) {
        logger.debug("[JWT][getRefreshClaims] start token");
        Claims claims = parseClaimsStrict(token, jwtRefreshSecret, "refresh");
        logger.debug("[JWT][getRefreshClaims] success subject={}", claims.getSubject());
        return claims;
    }
    public String generateAccessToken(EmployeeDTO employeeDTO) {
        logger.debug("[JWT][generateAccessToken] start user={}", employeeDTO.getEmail());

        String token = Jwts.builder()
                .claim("id", employeeDTO.getId())
                .claim("role", employeeDTO.getRole())
                .claim("type", "access")
                .setSubject(employeeDTO.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSigningKey(jwtAccessSecret), SignatureAlgorithm.HS256)
                .compact();

        logger.debug("[JWT][generateAccessToken] success token");
        return token;
    }
    public String generateRefreshToken(EmployeeDTO employeeDTO) {
        logger.debug("[JWT][generateRefreshToken] start user={}", employeeDTO.getEmail());

        String token = Jwts.builder()
                .setSubject(employeeDTO.getEmail())
                .claim("ver", UUID.randomUUID().toString())
                .claim("type", "refresh")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(jwtRefreshSecret), SignatureAlgorithm.HS256)
                .compact();

        logger.debug("[JWT][generateRefreshToken] success token");
        return token;
    }

    public boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        if (exp == null) {
            throw new JwtException("Token claims do not contain expiration");
        }
        return exp.before(new Date());
    }

    public boolean isAccess(Claims c) {
        return "access".equals(c.get("type"));
    }

    public boolean isRefresh(Claims c) {
        return "refresh".equals(c.get("type"));
    }
    public boolean isValidatedAccessToken(String accessToken) {
        logger.debug("[JWT][validateAccess] start token...");
        return isValidatedToken(accessToken, jwtAccessSecret, "access");
    }

    public boolean isValidatedRefreshToken( String refreshToken) {
        logger.debug("[JWT][validateRefresh] start token...");
        return isValidatedToken(refreshToken, jwtRefreshSecret, "refresh");
    }
    private boolean isValidatedToken(@NonNull String token, @NonNull String secretKey, @NonNull String tokenKind) {
        try {
            Claims claims = parseClaimsStrict(token, secretKey, tokenKind);
            if ("access".equals(tokenKind) && !isAccess(claims)) return false;
            if ("refresh".equals(tokenKind) && !isRefresh(claims)) return false;
            return !isExpired(claims);
        } catch (JwtException e) {
            logger.debug("[JWT][validate:{}] invalid reason={}", tokenKind, e.getMessage());
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
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds( accessExpiration));
        logger.debug("[JWT][cookie] access cookie prepared");
        return cookie;
    }
    public Cookie generateRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie("jwtRefresh", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(refreshExpiration));
        logger.debug("[JWT][cookie] refresh cookie prepared");
        return cookie;
    }

    private Claims parseClaimsStrict(String token, String secretKey, String tokenKind) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            logger.info("[JWT][parse:{}] expired token", tokenKind);
            throw new JwtException(tokenKind + " token is expired", e);

        } catch (UnsupportedJwtException e) {
            logger.warn("[JWT][parse:{}] unsupported token", tokenKind);
            throw new JwtException(tokenKind + " token format is unsupported", e);

        } catch (MalformedJwtException e) {
            logger.warn("[JWT][parse:{}] malformed token", tokenKind);
            throw new JwtException(tokenKind + " token is malformed", e);

        } catch (SecurityException | SignatureException e) {
            logger.warn("[JWT][parse:{}] bad signature token", tokenKind);
            throw new JwtException(tokenKind + " token signature is invalid", e);

        } catch (IllegalArgumentException e) {
            logger.warn("[JWT][parse:{}] empty/illegal token", tokenKind);
            throw new JwtException(tokenKind + " token is empty or illegal", e);

        } catch (JwtException e) {
            logger.warn("[JWT][parse:{}] generic jwt error token", tokenKind);
            throw new JwtException("Cannot parse " + tokenKind + " token", e);
        }
    }



}
