package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.service.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthRestController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/login
     * Вход в систему с email и паролем
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            JwtResponse tokens = authService.login(loginRequest);
            logger.info("User {} successfully logged in", loginRequest.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login successful", tokens));

        } catch (Exception e) {
            logger.warn("Login failed for {}: {}", loginRequest.getEmail(), e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/registration
     * Регистрация нового пользователя
     */

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody @Valid RegistrationDTO registrationDTO) {
        logger.info("Registration attempt for email: {}", registrationDTO.getEmail());

        try {
            EmployeeDTO employee = authService.registration(registrationDTO);
            logger.info("User {} successfully registered", registrationDTO.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Registration successful");
            response.put("data", new EmployeeDTO()); // Не отправляем все данные
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            logger.warn("Registration failed for {}: {}", registrationDTO.getEmail(), e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            // 409 Conflict - если пользователь уже существует
            if (e.getMessage().contains("Email")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * POST /api/v1/refresh
     * Обновление access token используя refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        logger.debug("Token refresh attempt");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("Refresh token is missing");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Refresh token is required");
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            JwtResponse tokens = authService.refresh(refreshToken);

            if (tokens.getAccessToken() == null || tokens.getRefreshToken() == null) {
                logger.warn("Token refresh failed: invalid tokens returned");
                throw new JwtException("Invalid tokens");
            }

            logger.debug("Token successfully refreshed");
            ApiResponse<JwtResponse> response = ApiResponse.success("Token refreshed", tokens);
            return ResponseEntity.ok(response);

        } catch (JwtException e) {
            logger.warn("JWT validation failed during refresh: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error("Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            ApiResponse<Void> errorResponse = ApiResponse.error("Token refresh failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        logger.info("User logout");
        response.addHeader("Set-Cookie", "jwtAccess=; Max-Age=0; Path=/; HttpOnly");
        response.addHeader("Set-Cookie", "jwtRefresh=; Max-Age=0; Path=/; HttpOnly");
        ApiResponse<Void> responseBody = ApiResponse.success("Logout successful");

        return ResponseEntity.ok(responseBody);
    }
}
