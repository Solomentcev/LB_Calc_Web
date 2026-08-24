package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.service.EmployeeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthentificationFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JwtService jwtService;
    private final EmployeeService employeeService;
    public JwtAuthentificationFilter(JwtService jwtService, EmployeeService employeeService) {
        this.jwtService = jwtService;
        this.employeeService = employeeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.debug("[JWT-FILTER] start uri={} method={}", request.getRequestURI(), request.getMethod());

        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                logger.debug("[JWT-FILTER] skip: security context already authenticated");
                filterChain.doFilter(request, response);
                return;
            }

            String accessToken = getAccessToken(request);

            if (accessToken == null || accessToken.isBlank()) {
                logger.debug("[JWT-FILTER] no access token provided");
                filterChain.doFilter(request, response);
                return;
            }

            processAccessOrRefresh(accessToken, request, response);

        } catch (JwtException e) {
            logger.warn("[JWT-FILTER] jwt error: {}", e.getMessage());
            clearAuthentication(response);
        } catch (Exception e) {
            logger.error("[JWT-FILTER] unexpected error", e);
            clearAuthentication(response);
        }

        filterChain.doFilter(request, response);
    }

    private void processAccessOrRefresh(String accessToken,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        logger.debug("[JWT-FILTER] process access token");

        try {
            Claims accessClaims = jwtService.getAccessClaims(accessToken);

            if (!jwtService.isAccess(accessClaims)) {
                throw new JwtException("Provided token is not access token");
            }

            if (jwtService.isExpired(accessClaims)) {
                logger.info("[JWT-FILTER] access token expired -> try refresh");
                tryRefresh(request, response);
                return;
            }

            String email = accessClaims.getSubject();
            if (email == null || email.isBlank()) {
                throw new JwtException("Access token subject(email) is missing");
            }

            authenticate(email);
            logger.debug("[JWT-FILTER] authenticated by access token");

        } catch (JwtException e) {

            logger.info("[JWT-FILTER] access rejected: {} -> try refresh", e.getMessage());
            tryRefresh(request, response);
        }
    }
    private void tryRefresh(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("[JWT-FILTER] refresh stage start");

        String refreshToken = getRefreshToken(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new JwtException("Refresh token is missing");
        }

        Claims refreshClaims = jwtService.getRefreshClaims(refreshToken);

        if (!jwtService.isRefresh(refreshClaims)) {
            throw new JwtException("Token type is not refresh");
        }

        if (jwtService.isExpired(refreshClaims)) {
            throw new JwtException("Refresh token is expired");
        }

        String userEmail = refreshClaims.getSubject();
        if (userEmail == null || userEmail.isBlank()) {
            throw new JwtException("Refresh token subject(email) is missing");
        }

        EmployeeDTO employeeDTO = employeeService.loadUserByEmail(userEmail);

        String newAccessToken = jwtService.generateAccessToken(employeeDTO);
        String newRefreshToken = jwtService.generateRefreshToken(employeeDTO);

        if (isRestRequest(request)) {
            response.setHeader("Authorization", "Bearer " + newAccessToken);
            response.setHeader("Refresh-Token", newRefreshToken);
            logger.debug("[JWT-FILTER] new tokens sent in headers (REST)");
        } else {
            response.addCookie(jwtService.generateAccessTokenCookie(newAccessToken));
            response.addCookie(jwtService.generateRefreshTokenCookie(newRefreshToken));
            logger.debug("[JWT-FILTER] new tokens sent in cookies (WEB)");
        }

        authenticate(userEmail);
        logger.info("[JWT-FILTER] user={} re-authenticated via refresh", userEmail);

    }

    private boolean isRestRequest(HttpServletRequest req) {
        String uri = req.getRequestURI();
        if (uri != null && uri.startsWith("/api")) return true;

        String requestedWith = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) return true;

        String accept = req.getHeader("Accept");
        String contentType = req.getContentType();

        return (accept != null && accept.contains("application/json"))
                || (contentType != null && contentType.contains("application/json"));
    }

    private String getAccessToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String value = header.substring(7).trim();
            return value.isEmpty() ? null : value;
        }
        return getTokenFromCookie(req, "jwtAccess");
    }

    private String getRefreshToken(HttpServletRequest req) {
        String header = req.getHeader("Refresh-Token");
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        return getTokenFromCookie(req, "jwtRefresh");
    }

    private String getTokenFromCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                String value = c.getValue();
                return (value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }


    private void authenticate(String userEmail) {
        EmployeeDTO employee = employeeService.loadUserByEmail(userEmail);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(employee, null, employee.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.debug("[JWT-FILTER] authentication success user={}", userEmail);
    }
    private void clearAuthentication(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        Cookie accessCookie = new Cookie("jwtAccess", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("jwtRefresh", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);

        logger.debug("[JWT-FILTER] security context cleared, auth cookies removed");
    }
}
