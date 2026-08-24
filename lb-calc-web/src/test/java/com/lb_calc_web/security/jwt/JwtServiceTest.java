package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.model.user.Role;
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
    private EmployeeDTO employeeDTO;
    private String accessSecret;
    private String refreshSecret;

    @BeforeEach
    void setUp()  {
        jwtService = new JwtService();
        accessSecret = Encoders.BASE64URL.encode(
                Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
        );
        refreshSecret = Encoders.BASE64URL.encode(
                Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded()
        );
        ReflectionTestUtils.setField(jwtService, "jwtAccessSecret", accessSecret);
        ReflectionTestUtils.setField(jwtService, "jwtRefreshSecret", refreshSecret);
        ReflectionTestUtils.setField(jwtService, "accessExpiration", 1000L * 60);     // 1 мин
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 1000L * 60 * 10); // 10 мин

        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(10L);
        employeeDTO.setEmail("test@mail.com");
        employeeDTO.setRole(Role.ROLE_ADMIN);
    }

    @Test
    void generateAndParseAccessToken_success() {
        String token = jwtService.generateAccessToken(employeeDTO);
        Claims claims = jwtService.getAccessClaims(token);

        assertEquals("test@mail.com", claims.getSubject());
        assertEquals("access", claims.get("type"));
        assertEquals(10L, ((Number) claims.get("id")).longValue());
        assertFalse(jwtService.isExpired(claims));
        assertTrue(jwtService.isAccess(claims));
    }

    @Test
    void generateAndParseRefreshToken_success() {
        String token = jwtService.generateRefreshToken(employeeDTO);
        Claims claims = jwtService.getRefreshClaims(token);

        assertEquals("test@mail.com", claims.getSubject());
        assertEquals("refresh", claims.get("type"));
        assertNotNull(claims.get("ver"));
        assertFalse(jwtService.isExpired(claims));
        assertTrue(jwtService.isRefresh(claims));
    }

    @Test
    void extractEmail_success() {
        String token = jwtService.generateAccessToken(employeeDTO);
        assertEquals("test@mail.com", jwtService.extractEmail(token));
    }

    @Test
    void validateAccessToken_trueForValidToken() {
        String token = jwtService.generateAccessToken(employeeDTO);
        assertTrue(jwtService.isValidatedAccessToken(token));
    }

    @Test
    void validateRefreshToken_trueForValidToken() {
        String token = jwtService.generateRefreshToken(employeeDTO);
        assertTrue(jwtService.isValidatedRefreshToken(token));
    }

    @Test
    void getAccessClaims_failForRefreshToken() {
        String refresh = jwtService.generateRefreshToken(employeeDTO);

        // Сигнатура будет неврной для access secret => JwtException
        assertThrows(JwtException.class, () -> jwtService.getAccessClaims(refresh));
    }

    @Test
    void getRefreshClaims_failForAccessToken() {
        String access = jwtService.generateAccessToken(employeeDTO);

        // Сигнатура будет неверной для refresh secret => JwtException
        assertThrows(JwtException.class, () -> jwtService.getRefreshClaims(access));
    }

    @Test
    void validateAccessToken_falseForGarbage() {
        assertFalse(jwtService.isValidatedAccessToken("not-a-jwt"));
    }

    @Test
    void validateRefreshToken_falseForGarbage() {
        assertFalse(jwtService.isValidatedRefreshToken("not-a-jwt"));
    }

    @Test
    void accessCookie_hasExpectedFlags() {
        var cookie = jwtService.generateAccessTokenCookie("token1");

        assertEquals("jwtAccess", cookie.getName());
        assertTrue(cookie.isHttpOnly());
     //   assertTrue(cookie.getSecure()); /
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.getMaxAge() > 0);
    }

    @Test
    void refreshCookie_hasExpectedFlags() {
        var cookie = jwtService.generateRefreshTokenCookie("token2");

        assertEquals("jwtRefresh", cookie.getName());
        assertTrue(cookie.isHttpOnly());
        //assertTrue(cookie.getSecure()); /
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.getMaxAge() > 0);
    }
}