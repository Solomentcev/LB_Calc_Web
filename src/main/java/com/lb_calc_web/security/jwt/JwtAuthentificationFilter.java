package com.lb_calc_web.security.jwt;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthentificationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final EmployeeService employeeService;
    public JwtAuthentificationFilter(JwtService jwtService, EmployeeService employeeService) {
        this.jwtService = jwtService;
        this.employeeService = employeeService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("JWT Authentication Filtering...");
        try {
            String accessToken= getAccessToken(request);
            if(accessToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    try {
                        Claims claims= jwtService.getAccessClaims(accessToken);
                        if(jwtService.isAccess(claims)) {
                            String userEmail = claims.getSubject();
                            authenticate(userEmail);
                        }
                    } catch (ExpiredJwtException e) {
                       Claims claims=e.getClaims();
                        if(jwtService.isAccess(claims)) {
                            tryRefresh(request, response);
                        }
                    }
                } catch (JwtException e) {
                    logger.warn("JWT Token Invalid " +e.getMessage());
                }
            } else logger.debug("JWT Token is null or User is authentificated");
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
    private void tryRefresh(HttpServletRequest req, HttpServletResponse res) {

        String refreshToken = getRefreshToken(req);

        if (refreshToken == null ) return;

        try {
            Claims claims = jwtService.getRefreshClaims(refreshToken);
            if(!jwtService.isRefresh(claims)) return;
            String userEmail = claims.getSubject();
            EmployeeDTO employeeDTO=employeeService.loadUserByEmail(userEmail);
            String newAccessToken= jwtService.generateAccessToken(employeeDTO);
            String newRefreshToken= jwtService.generateRefreshToken(employeeDTO);
            boolean isRest=isRestRequest(req);
            if (!isRest) {
                res.addCookie(jwtService.generateAccessTokenCookie(newAccessToken));
                res.addCookie(jwtService.generateRefreshTokenCookie(newRefreshToken));
            } else {
                res.setHeader("Authorization", "Bearer " + newAccessToken);
                res.setHeader("Refresh-Token", newRefreshToken);
            }
            authenticate(userEmail);
        }catch (ExpiredJwtException e) {
            logger.info("Refresh token expired → user needs re-login"+e.getMessage());

        } catch (JwtException e) {
            logger.warn("Invalid refresh token → possible attack"+e.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected refresh error", e);
        }

    }

    private boolean isRestRequest(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String contentType = req.getContentType();
        String requestedWith = req.getHeader("X-Requested-With");
        String uri = req.getRequestURI();

        // 1. API paths (самый надёжный способ)
        if (uri != null && uri.startsWith("/api")) {
            return true;
        }

        // 2. AJAX / fetch requests
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }

        // 3. JSON requests (Accept / Content-Type)
        if ((accept != null && accept.contains("application/json")) ||
                (contentType != null && contentType.contains("application/json"))) {
            return true;
        }

        // fallback → считаем UI
        return false;
    }

    private String getAccessToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);

        return getTokenFromCookie(req, "jwtAccess");
    }

    private String getRefreshToken(HttpServletRequest req) {
        String h = req.getHeader("Refresh-Token");
        if (h != null) return h;

        return getTokenFromCookie(req, "jwtRefresh");
    }

    private String getTokenFromCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }


    private void authenticate(String userEmail) {
        EmployeeDTO employee = this.employeeService.loadUserByEmail(userEmail);
        logger.debug("Аутентификация...");
        logger.debug("User: " + employee);
        logger.debug("Email: " + userEmail);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                employee,
                null,
                employee.getAuthorities()
                //  List.of(new SimpleGrantedAuthority(employee.getRole().name())));
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.debug("Authentication Success "+authToken);
    }
}
