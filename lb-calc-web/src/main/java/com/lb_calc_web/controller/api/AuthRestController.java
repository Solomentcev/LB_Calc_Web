package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        
            JwtResponse tokens = authService.login(loginRequest);
            logger.info("User {} successfully logged in", loginRequest.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login successful", tokens));

        
    }

    /**
     * POST /api/v1/registration
     * Регистрация нового пользователя
     */

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody @Valid RegistrationDTO registrationDTO) {
        logger.info("Registration attempt for email: {}", registrationDTO.getEmail());

        
            EmployeeDTO employee = authService.registration(registrationDTO);
            logger.info("User {} successfully registered", registrationDTO.getEmail());
            ApiResponse<EmployeeDTO> response = ApiResponse.success("Registration successful", employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        
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
            ApiResponse<Void> errorResponse = ApiResponse.error("Refresh token is missing");
                       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        
            JwtResponse tokens = authService.refresh(refreshToken);

            logger.debug("Token successfully refreshed");
            ApiResponse<JwtResponse> response = ApiResponse.success("Token refreshed", tokens);
            return ResponseEntity.ok(response);

        
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
