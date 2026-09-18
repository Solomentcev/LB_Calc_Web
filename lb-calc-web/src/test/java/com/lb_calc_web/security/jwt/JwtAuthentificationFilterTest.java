package com.lb_calc_web.security.jwt;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.security.EmployeePrincipal;
import com.lb_calc_web.service.EmployeeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthentificationFilterTest {

    private JwtService jwtService;
    private EmployeeService employeeService;
    private JwtAuthentificationFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        employeeService = mock(EmployeeService.class);
        filter =
                new JwtAuthentificationFilter(
                        jwtService,
                        employeeService
                );
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    private EmployeePrincipal principal(String email) {
        return new EmployeePrincipal(
                10L,
                email,
                "HASH",
                Role.ROLE_MANAGER
        );
    }

    private Claims claims(
            String subject,
            String type,
            Date expiration
    ) {
        Claims claims = new DefaultClaims();
        claims.setSubject(subject);
        claims.put("type", type);
        claims.setExpiration(expiration);
        return claims;
    }

    @Test
    void missingAccessToken_shouldContinueWithoutAuthentication()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/lbcs"
                );
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void validAccessToken_shouldAuthenticatePrincipal()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/lbcs"
                );
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer access-ok"
        );

        Claims accessClaims =
                claims(
                        "u@test.local",
                        "access",
                        new Date(
                                System.currentTimeMillis()
                                        + 60_000
                        )
                );

        EmployeePrincipal principal =
                principal("u@test.local");

        when(jwtService.getAccessClaims("access-ok"))
                .thenReturn(accessClaims);
        when(jwtService.isAccess(accessClaims))
                .thenReturn(true);
        when(jwtService.isExpired(accessClaims))
                .thenReturn(false);
        when(employeeService.loadUserByUsername("u@test.local"))
                .thenReturn(principal);

        filter.doFilter(request, response, chain);

        assertSame(
                principal,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
        );
        verify(chain).doFilter(request, response);
    }

    @Test
    void expiredAccess_withRefresh_restRequest_shouldRenewHeaders()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/lbcs"
                );
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer access-expired"
        );
        request.addHeader(
                "Refresh-Token",
                "refresh-ok"
        );

        Claims refreshClaims =
                claims(
                        "rest@test.local",
                        "refresh",
                        new Date(
                                System.currentTimeMillis()
                                        + 60_000
                        )
                );

        EmployeePrincipal principal =
                principal("rest@test.local");

        when(jwtService.getAccessClaims("access-expired"))
                .thenThrow(
                        new JwtException("expired")
                );
        when(jwtService.getRefreshClaims("refresh-ok"))
                .thenReturn(refreshClaims);
        when(jwtService.isRefresh(refreshClaims))
                .thenReturn(true);
        when(jwtService.isExpired(refreshClaims))
                .thenReturn(false);
        when(employeeService.loadUserByUsername("rest@test.local"))
                .thenReturn(principal);
        when(jwtService.generateAccessToken(principal))
                .thenReturn("new-access");
        when(jwtService.generateRefreshToken(principal))
                .thenReturn("new-refresh");

        filter.doFilter(request, response, chain);

        assertSame(
                principal,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
        );
        assertEquals(
                "Bearer new-access",
                response.getHeader("Authorization")
        );
        assertEquals(
                "new-refresh",
                response.getHeader("Refresh-Token")
        );
        verify(chain).doFilter(request, response);
    }

    @Test
    void expiredAccess_withRefresh_webRequest_shouldRenewCookies()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/myprofile"
                );
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.setCookies(
                new Cookie("jwtAccess", "access-expired"),
                new Cookie("jwtRefresh", "refresh-ok")
        );

        Claims refreshClaims =
                claims(
                        "web@test.local",
                        "refresh",
                        new Date(
                                System.currentTimeMillis()
                                        + 60_000
                        )
                );

        EmployeePrincipal principal =
                principal("web@test.local");

        when(jwtService.getAccessClaims("access-expired"))
                .thenThrow(
                        new JwtException("expired")
                );
        when(jwtService.getRefreshClaims("refresh-ok"))
                .thenReturn(refreshClaims);
        when(jwtService.isRefresh(refreshClaims))
                .thenReturn(true);
        when(jwtService.isExpired(refreshClaims))
                .thenReturn(false);
        when(employeeService.loadUserByUsername("web@test.local"))
                .thenReturn(principal);
        when(jwtService.generateAccessToken(principal))
                .thenReturn("new-access");
        when(jwtService.generateRefreshToken(principal))
                .thenReturn("new-refresh");
        when(jwtService.generateAccessTokenCookie("new-access"))
                .thenReturn(
                        new Cookie(
                                "jwtAccess",
                                "new-access"
                        )
                );
        when(jwtService.generateRefreshTokenCookie("new-refresh"))
                .thenReturn(
                        new Cookie(
                                "jwtRefresh",
                                "new-refresh"
                        )
                );

        filter.doFilter(request, response, chain);

        Cookie[] cookies = response.getCookies();

        assertTrue(
                java.util.Arrays.stream(cookies)
                        .anyMatch(
                                cookie ->
                                        "jwtAccess".equals(
                                                cookie.getName()
                                        )
                        )
        );

        assertTrue(
                java.util.Arrays.stream(cookies)
                        .anyMatch(
                                cookie ->
                                        "jwtRefresh".equals(
                                                cookie.getName()
                                        )
                        )
        );

        assertNull(
                response.getHeader("Authorization")
        );
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidRefresh_shouldClearAuthenticationAndCookies()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/lbcs"
                );
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.setCookies(
                new Cookie("jwtAccess", "bad-access"),
                new Cookie("jwtRefresh", "bad-refresh")
        );

        when(jwtService.getAccessClaims("bad-access"))
                .thenThrow(new JwtException("invalid"));
        when(jwtService.getRefreshClaims("bad-refresh"))
                .thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        assertTrue(
                java.util.Arrays.stream(
                                response.getCookies()
                        )
                        .filter(
                                cookie ->
                                        "jwtAccess".equals(
                                                cookie.getName()
                                        )
                                                || "jwtRefresh".equals(
                                                cookie.getName()
                                        )
                        )
                        .allMatch(
                                cookie ->
                                        cookie.getMaxAge() == 0
                        )
        );

        verify(chain).doFilter(request, response);
    }
}
