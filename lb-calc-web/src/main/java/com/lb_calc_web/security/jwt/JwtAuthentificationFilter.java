package com.lb_calc_web.security.jwt;

import com.lb_calc_web.security.EmployeePrincipal;
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

/** Фильтр проверки JWT, восстановления сессии и установки аутентификации в SecurityContext. */
@Component
public class JwtAuthentificationFilter
        extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    JwtAuthentificationFilter.class
            );

    private final JwtService jwtService;
    private final EmployeeService employeeService;

    public JwtAuthentificationFilter(
            JwtService jwtService,
            EmployeeService employeeService
    ) {
        this.jwtService = jwtService;
        this.employeeService = employeeService;
    }

    /** Извлекает токен запроса, восстанавливает аутентификацию и продолжает цепочку фильтров. */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String accessToken = getAccessToken(request);
            if (accessToken != null && !accessToken.isBlank()) {
                try {
                    processAccessOrRefresh(accessToken, request, response);
                } catch (JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException exception) {
                    logger.debug("JWT отклонён: тип ошибки={}",
                            exception.getClass().getSimpleName());
                    clearAuthentication(response);
                } catch (RuntimeException exception) {
                    logger.error("Ошибка восстановления аутентификации по JWT", exception);
                    throw exception;
                }
            }
        }

        // Цепочка вызывается ровно один раз. Ошибки следующих фильтров и контроллеров
        // не должны маскироваться под ошибку JWT или приводить к повторному выполнению запроса.
        filterChain.doFilter(request, response);
    }

    /** Проверяет access token и при необходимости инициирует обновление по refresh token. */
    private void processAccessOrRefresh(
            String accessToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        try {

            Claims accessClaims =
                    jwtService.getAccessClaims(
                            accessToken
                    );

            if (!jwtService.isAccess(
                    accessClaims
            )) {

                throw new JwtException(
                        "Provided token is not access token"
                );
            }

            if (jwtService.isExpired(
                    accessClaims
            )) {

                tryRefresh(
                        request,
                        response
                );

                return;
            }

            String email =
                    accessClaims.getSubject();

            if (email == null || email.isBlank()) {

                throw new JwtException(
                        "Access token subject is missing"
                );
            }

            authenticate(email);

        } catch (JwtException e) {

            logger.debug(
                    "Access token отклонён: тип ошибки={}",
                    e.getClass().getSimpleName()
            );

            tryRefresh(
                    request,
                    response
            );
        }
    }

    /** Проверяет refresh token, выпускает новую пару токенов и обновляет данные ответа. */
    private void tryRefresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken =
                getRefreshToken(request);

        if (refreshToken == null
                || refreshToken.isBlank()) {

            throw new JwtException(
                    "Refresh token is missing"
            );
        }

        Claims refreshClaims =
                jwtService.getRefreshClaims(
                        refreshToken
                );

        if (!jwtService.isRefresh(
                refreshClaims
        )) {

            throw new JwtException(
                    "Token type is not refresh"
            );
        }

        if (jwtService.isExpired(
                refreshClaims
        )) {

            throw new JwtException(
                    "Refresh token is expired"
            );
        }

        String email =
                refreshClaims.getSubject();

        if (email == null || email.isBlank()) {

            throw new JwtException(
                    "Refresh token subject is missing"
            );
        }

        EmployeePrincipal principal =
                employeeService.loadUserByUsername(
                        email
                );

        if (!principal.isEnabled()) {
            throw new JwtException(
                    "Учётная запись деактивирована"
            );
        }

        String newAccessToken =
                jwtService.generateAccessToken(
                        principal
                );

        String newRefreshToken =
                jwtService.generateRefreshToken(
                        principal
                );

        if (isRestRequest(request)) {

            response.setHeader(
                    "Authorization",
                    "Bearer " + newAccessToken
            );

            response.setHeader(
                    "Refresh-Token",
                    newRefreshToken
            );

        } else {

            response.addCookie(
                    jwtService.generateAccessTokenCookie(
                            newAccessToken
                    )
            );

            response.addCookie(
                    jwtService.generateRefreshTokenCookie(
                            newRefreshToken
                    )
            );
        }

        authenticate(email);
    }

    /** Загружает активную учётную запись и устанавливает аутентификацию в SecurityContext. */
    private void authenticate(
            String email
    ) {

        EmployeePrincipal principal =
                employeeService.loadUserByUsername(
                        email
                );

        if (!principal.isEnabled()) {
            throw new JwtException(
                    "Учётная запись деактивирована"
            );
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    /** Определяет, ожидает ли клиент REST-ответ с токенами в заголовках. */
    private boolean isRestRequest(
            HttpServletRequest request
    ) {

        String uri =
                request.getRequestURI();

        if (uri != null
                && uri.startsWith("/api")) {

            return true;
        }

        String requestedWith =
                request.getHeader(
                        "X-Requested-With"
                );

        if ("XMLHttpRequest".equalsIgnoreCase(
                requestedWith
        )) {

            return true;
        }

        String accept =
                request.getHeader("Accept");

        String contentType =
                request.getContentType();

        return (accept != null
                && accept.contains(
                "application/json"
        ))
                || (contentType != null
                && contentType.contains(
                "application/json"
        ));
    }

    /** Извлекает access token из Authorization или cookie. */
    private String getAccessToken(
            HttpServletRequest request
    ) {

        String header =
                request.getHeader("Authorization");

        if (header != null
                && header.startsWith("Bearer ")) {

            String value =
                    header.substring(7).trim();

            return value.isEmpty()
                    ? null
                    : value;
        }

        return getTokenFromCookie(
                request,
                "jwtAccess"
        );
    }

    /** Извлекает refresh token из заголовка или cookie. */
    private String getRefreshToken(
            HttpServletRequest request
    ) {

        String header =
                request.getHeader("Refresh-Token");

        if (header != null
                && !header.isBlank()) {

            return header.trim();
        }

        return getTokenFromCookie(
                request,
                "jwtRefresh"
        );
    }

    /** Находит непустое значение cookie с заданным именем. */
    private String getTokenFromCookie(
            HttpServletRequest request,
            String name
    ) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (name.equals(cookie.getName())) {

                String value =
                        cookie.getValue();

                return value == null
                        || value.isBlank()
                        ? null
                        : value;
            }
        }

        return null;
    }

    /** Очищает SecurityContext и удаляет токены из cookie. */
    private void clearAuthentication(
            HttpServletResponse response
    ) {

        SecurityContextHolder.clearContext();

        Cookie accessCookie =
                new Cookie("jwtAccess", null);

        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        accessCookie.setHttpOnly(true);

        response.addCookie(accessCookie);

        Cookie refreshCookie =
                new Cookie("jwtRefresh", null);

        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true);

        response.addCookie(refreshCookie);
    }
}