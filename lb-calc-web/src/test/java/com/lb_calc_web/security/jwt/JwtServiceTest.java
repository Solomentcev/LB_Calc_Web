package com.lb_calc_web.security.jwt;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.security.EmployeePrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private EmployeePrincipal principal;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String accessSecret =
                Encoders.BASE64URL.encode(
                        Keys.secretKeyFor(
                                SignatureAlgorithm.HS256
                        ).getEncoded()
                );

        String refreshSecret =
                Encoders.BASE64URL.encode(
                        Keys.secretKeyFor(
                                SignatureAlgorithm.HS256
                        ).getEncoded()
                );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtAccessSecret",
                accessSecret
        );

        ReflectionTestUtils.setField(
                jwtService,
                "jwtRefreshSecret",
                refreshSecret
        );

        ReflectionTestUtils.setField(
                jwtService,
                "accessExpiration",
                60_000L
        );

        ReflectionTestUtils.setField(
                jwtService,
                "refreshExpiration",
                600_000L
        );

        principal =
                new EmployeePrincipal(
                        10L,
                        "test@mail.com",
                        "HASH",
                        Role.ROLE_ADMIN
                );
    }

    @Test
    void generateAndParseAccessToken_shouldPreserveClaims() {
        String token =
                jwtService.generateAccessToken(
                        principal
                );

        Claims claims =
                jwtService.getAccessClaims(token);

        assertEquals(
                "test@mail.com",
                claims.getSubject()
        );
        assertEquals(
                "access",
                claims.get("type")
        );
        assertEquals(
                10L,
                ((Number) claims.get("id")).longValue()
        );
        assertEquals(
                Role.ROLE_ADMIN.name(),
                claims.get("role")
        );
        assertFalse(jwtService.isExpired(claims));
        assertTrue(jwtService.isAccess(claims));
    }

    @Test
    void generateAndParseRefreshToken_shouldPreserveClaims() {
        String token =
                jwtService.generateRefreshToken(
                        principal
                );

        Claims claims =
                jwtService.getRefreshClaims(token);

        assertEquals(
                "test@mail.com",
                claims.getSubject()
        );
        assertEquals(
                "refresh",
                claims.get("type")
        );
        assertNotNull(claims.get("ver"));
        assertFalse(jwtService.isExpired(claims));
        assertTrue(jwtService.isRefresh(claims));
    }

    @Test
    void extractEmail_shouldReturnSubject() {
        String token =
                jwtService.generateAccessToken(
                        principal
                );

        assertEquals(
                "test@mail.com",
                jwtService.extractEmail(token)
        );
    }

    @Test
    void invalidToken_shouldReturnFalseFromValidation() {
        assertFalse(
                jwtService.isValidatedAccessToken(
                        "not-a-jwt"
                )
        );

        assertFalse(
                jwtService.isValidatedRefreshToken(
                        "not-a-jwt"
                )
        );
    }

    @Test
    void wrongTokenKind_shouldBeRejected() {
        String access =
                jwtService.generateAccessToken(
                        principal
                );

        String refresh =
                jwtService.generateRefreshToken(
                        principal
                );

        assertThrows(
                JwtException.class,
                () -> jwtService.getRefreshClaims(access)
        );

        assertThrows(
                JwtException.class,
                () -> jwtService.getAccessClaims(refresh)
        );
    }

    @Test
    void accessCookie_shouldHaveExpectedFlags() {
        var cookie =
                jwtService.generateAccessTokenCookie(
                        "token1"
                );

        assertEquals(
                "jwtAccess",
                cookie.getName()
        );
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.getMaxAge() > 0);
    }

    @Test
    void refreshCookie_shouldHaveExpectedFlags() {
        var cookie =
                jwtService.generateRefreshTokenCookie(
                        "token2"
                );

        assertEquals(
                "jwtRefresh",
                cookie.getName()
        );
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.getMaxAge() > 0);
    }
}
