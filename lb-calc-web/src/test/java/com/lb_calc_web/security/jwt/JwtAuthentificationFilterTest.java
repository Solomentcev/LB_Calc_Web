package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.model.user.Role;
import com.lb_calc_web.service.EmployeeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
        filter = new JwtAuthentificationFilter(jwtService, employeeService);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    private EmployeeDTO employee(String email) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmail(email);
        dto.setRole(Role.ROLE_MANAGER);
        return dto;
    }

    private Claims claims(String subject, String type, Date exp) {
        Claims c = new DefaultClaims();
        c.setSubject(subject);
        c.put("type", type);
        c.setExpiration(exp);
        return c;
    }

    @Test
    void noAccessToken_chainContinues_noAuth() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/lbs");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, employeeService);
    }

    @Test
    void validAccessToken_authenticatesUser() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/lbs");
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Bearer access-ok");

        Claims accessClaims = claims("u@test.local", "access", new Date(System.currentTimeMillis() + 60_000));

        when(jwtService.getAccessClaims("access-ok")).thenReturn(accessClaims);
        when(jwtService.isAccess(accessClaims)).thenReturn(true);
        when(jwtService.isExpired(accessClaims)).thenReturn(false);
        when(employeeService.loadUserByEmail("u@test.local")).thenReturn(employee("u@test.local"));

        filter.doFilter(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("u@test.local",
                ((EmployeeDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        verify(chain).doFilter(req, res);
    }

    @Test
    void expiredAccess_withValidRefresh_restFlow_setsHeadersAndAuthenticates() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/lbs"); // REST path
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.setCookies(
                new Cookie("jwtAccess", "access-expired"),
                new Cookie("jwtRefresh", "refresh-ok")
        );
        req.addHeader("Accept", "application/json");

        Claims refreshClaims = claims("rest@test.local", "refresh", new Date(System.currentTimeMillis() + 60_000));

        when(jwtService.getAccessClaims("access-expired"))
                .thenThrow(new JwtException("access expired"));

        when(jwtService.getRefreshClaims("refresh-ok")).thenReturn(refreshClaims);
        when(jwtService.isRefresh(refreshClaims)).thenReturn(true);
        when(jwtService.isExpired(refreshClaims)).thenReturn(false);

        when(employeeService.loadUserByEmail("rest@test.local")).thenReturn(employee("rest@test.local"));
        when(jwtService.generateAccessToken(any(EmployeeDTO.class))).thenReturn("new-access-rest");
        when(jwtService.generateRefreshToken(any(EmployeeDTO.class))).thenReturn("new-refresh-rest");

        filter.doFilter(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("rest@test.local",
                ((EmployeeDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());

        // Для REST токены идут в headers
        assertEquals("Bearer new-access-rest", res.getHeader("Authorization"));
        assertEquals("new-refresh-rest", res.getHeader("Refresh-Token"));

        // И не обязаны идти в cookies
        assertTrue(res.getCookies() == null || res.getCookies().length == 0);

        verify(chain).doFilter(req, res);
    }
    @Test
    void expiredAccess_withValidRefresh_webFlow_setsCookiesAndAuthenticates() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/myprofile"); // WEB path (не /api)
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.setCookies(
                new Cookie("jwtAccess", "access-expired"),
                new Cookie("jwtRefresh", "refresh-ok")
        );
        req.addHeader("Accept", "text/html");

        Claims refreshClaims = claims("web@test.local", "refresh", new Date(System.currentTimeMillis() + 60_000));

        when(jwtService.getAccessClaims("access-expired"))
                .thenThrow(new JwtException("access expired"));

        when(jwtService.getRefreshClaims("refresh-ok")).thenReturn(refreshClaims);
        when(jwtService.isRefresh(refreshClaims)).thenReturn(true);
        when(jwtService.isExpired(refreshClaims)).thenReturn(false);

        when(employeeService.loadUserByEmail("web@test.local")).thenReturn(employee("web@test.local"));
        when(jwtService.generateAccessToken(any(EmployeeDTO.class))).thenReturn("new-access-web");
        when(jwtService.generateRefreshToken(any(EmployeeDTO.class))).thenReturn("new-refresh-web");

        when(jwtService.generateAccessTokenCookie("new-access-web"))
                .thenReturn(new Cookie("jwtAccess", "new-access-web"));
        when(jwtService.generateRefreshTokenCookie("new-refresh-web"))
                .thenReturn(new Cookie("jwtRefresh", "new-refresh-web"));

        filter.doFilter(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("web@test.local",
                ((EmployeeDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());

        // Для WEB токены идут в cookies
        Cookie[] out = res.getCookies();
        assertNotNull(out);
        assertTrue(out.length >= 2);

        boolean hasAccess = java.util.Arrays.stream(out)
                .anyMatch(c -> "jwtAccess".equals(c.getName()) && "new-access-web".equals(c.getValue()));
        boolean hasRefresh = java.util.Arrays.stream(out)
                .anyMatch(c -> "jwtRefresh".equals(c.getName()) && "new-refresh-web".equals(c.getValue()));

        assertTrue(hasAccess);
        assertTrue(hasRefresh);

        // И headers не обязательны
        assertNull(res.getHeader("Authorization"));
        assertNull(res.getHeader("Refresh-Token"));

        verify(chain).doFilter(req, res);
    }
    @Test
    void invalidRefresh_clearsAuthenticationAndDeletesCookies() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/lbs");
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.setCookies(
                new Cookie("jwtAccess", "bad-access"),
                new Cookie("jwtRefresh", "bad-refresh")
        );

        when(jwtService.getAccessClaims("bad-access"))
                .thenThrow(new JwtException("invalid access"));

        when(jwtService.getRefreshClaims("bad-refresh"))
                .thenThrow(new JwtException("invalid refresh"));

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        Cookie[] out = res.getCookies();
        assertNotNull(out);
        // ожидаем cookies удаления (maxAge=0)
        assertTrue(
                java.util.Arrays.stream(out)
                        .filter(c -> "jwtAccess".equals(c.getName()) || "jwtRefresh".equals(c.getName()))
                        .allMatch(c -> c.getMaxAge() == 0)
        );
        verify(chain).doFilter(req, res);
    }

    @Test
    void restRequest_putsNewTokensInHeaders() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/projects");
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("Authorization", "Bearer bad-access");
        req.addHeader("Refresh-Token", "refresh-ok");
        req.addHeader("Accept", "application/json");

        Claims refreshClaims = claims("api@test.local", "refresh", new Date(System.currentTimeMillis() + 60_000));

        when(jwtService.getAccessClaims("bad-access"))
                .thenThrow(new JwtException("expired"));
        when(jwtService.getRefreshClaims("refresh-ok")).thenReturn(refreshClaims);
        when(jwtService.isRefresh(refreshClaims)).thenReturn(true);
        when(jwtService.isExpired(refreshClaims)).thenReturn(false);

        when(employeeService.loadUserByEmail("api@test.local")).thenReturn(employee("api@test.local"));
        when(jwtService.generateAccessToken(ArgumentMatchers.any(EmployeeDTO.class))).thenReturn("access-new");
        when(jwtService.generateRefreshToken(ArgumentMatchers.any(EmployeeDTO.class))).thenReturn("refresh-new");

        filter.doFilter(req, res, chain);

        assertEquals("Bearer access-new", res.getHeader("Authorization"));
        assertEquals("refresh-new", res.getHeader("Refresh-Token"));
        verify(chain).doFilter(req, res);
    }
}