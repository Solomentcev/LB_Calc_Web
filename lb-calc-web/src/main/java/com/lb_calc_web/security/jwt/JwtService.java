package com.lb_calc_web.security.jwt;

import com.lb_calc_web.security.EmployeePrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
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

    private static final Logger logger =
            LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret.access}")
    private String jwtAccessSecret;

    @Value("${jwt.secret.refresh}")
    private String jwtRefreshSecret;

    @Value("${jwt.expiration.access}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpiration;

    public String extractEmail(String token) {

        Claims claims =
                getAccessClaims(token);

        String subject =
                claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new JwtException(
                    "Access token does not contain subject"
            );
        }

        return subject;
    }

    public Claims getAccessClaims(String token) {

        return parseClaimsStrict(
                token,
                jwtAccessSecret,
                "access"
        );
    }

    public Claims getRefreshClaims(String token) {

        return parseClaimsStrict(
                token,
                jwtRefreshSecret,
                "refresh"
        );
    }

    public String generateAccessToken(
            EmployeePrincipal principal
    ) {

        return Jwts.builder()
                .claim("id", principal.getId())
                .claim("role", principal.getRole())
                .claim("type", "access")
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + accessExpiration
                        )
                )
                .signWith(
                        getSigningKey(jwtAccessSecret),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public String generateRefreshToken(
            EmployeePrincipal principal
    ) {

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim(
                        "ver",
                        UUID.randomUUID().toString()
                )
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + refreshExpiration
                        )
                )
                .signWith(
                        getSigningKey(jwtRefreshSecret),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    public boolean isExpired(Claims claims) {

        Date expiration =
                claims.getExpiration();

        if (expiration == null) {
            throw new JwtException(
                    "Token claims do not contain expiration"
            );
        }

        return expiration.before(new Date());
    }

    public boolean isAccess(Claims claims) {

        return "access".equals(
                claims.get("type")
        );
    }

    public boolean isRefresh(Claims claims) {

        return "refresh".equals(
                claims.get("type")
        );
    }

    public boolean isValidatedAccessToken(
            String accessToken
    ) {

        return isValidatedToken(
                accessToken,
                jwtAccessSecret,
                "access"
        );
    }

    public boolean isValidatedRefreshToken(
            String refreshToken
    ) {

        return isValidatedToken(
                refreshToken,
                jwtRefreshSecret,
                "refresh"
        );
    }

    private boolean isValidatedToken(
            @NonNull String token,
            @NonNull String secretKey,
            @NonNull String tokenKind
    ) {

        try {

            Claims claims =
                    parseClaimsStrict(
                            token,
                            secretKey,
                            tokenKind
                    );

            if ("access".equals(tokenKind)
                    && !isAccess(claims)) {

                return false;
            }

            if ("refresh".equals(tokenKind)
                    && !isRefresh(claims)) {

                return false;
            }

            return !isExpired(claims);

        } catch (JwtException e) {

            logger.debug(
                    "[JWT][validate:{}] invalid token",
                    tokenKind
            );

            return false;
        }
    }

    private Key getSigningKey(
            String secretKey
    ) {

        byte[] keyBytes =
                Decoders.BASE64URL.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Cookie generateAccessTokenCookie(
            String token
    ) {

        Cookie cookie =
                new Cookie("jwtAccess", token);

        cookie.setHttpOnly(true);
        cookie.setPath("/");

        cookie.setMaxAge(
                (int) TimeUnit.MILLISECONDS.toSeconds(
                        accessExpiration
                )
        );

        return cookie;
    }

    public Cookie generateRefreshTokenCookie(
            String token
    ) {

        Cookie cookie =
                new Cookie("jwtRefresh", token);

        cookie.setHttpOnly(true);
        cookie.setPath("/");

        cookie.setMaxAge(
                (int) TimeUnit.MILLISECONDS.toSeconds(
                        refreshExpiration
                )
        );

        return cookie;
    }

    private Claims parseClaimsStrict(
            String token,
            String secretKey,
            String tokenKind
    ) {

        try {

            return Jwts.parserBuilder()
                    .setSigningKey(
                            getSigningKey(secretKey)
                    )
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {

            logger.debug(
                    "[JWT][parse:{}] expired token",
                    tokenKind
            );

            throw new JwtException(
                    tokenKind + " token is expired",
                    e
            );

        } catch (UnsupportedJwtException e) {

            throw new JwtException(
                    tokenKind + " token format is unsupported",
                    e
            );

        } catch (MalformedJwtException e) {

            throw new JwtException(
                    tokenKind + " token is malformed",
                    e
            );

        } catch (SecurityException | SignatureException e) {

            throw new JwtException(
                    tokenKind + " token signature is invalid",
                    e
            );

        } catch (IllegalArgumentException e) {

            throw new JwtException(
                    tokenKind + " token is empty or illegal",
                    e
            );

        } catch (JwtException e) {

            throw new JwtException(
                    "Cannot parse " + tokenKind + " token",
                    e
            );
        }
    }
}