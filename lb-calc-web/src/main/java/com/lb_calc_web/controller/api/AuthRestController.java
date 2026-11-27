package com.lb_calc_web.controller.api;

import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.security.jwt.AuthTokens;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST-контроллер аутентификации, регистрации, обновления токенов и выхода пользователя. */
@RestController
@RequestMapping("/api/v1")
public class AuthRestController {
    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    /** Выполняет вход по учётным данным и возвращает пару JWT-токенов. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) throws AuthException {
        AuthTokens tokens = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        JwtResponse response = new JwtResponse(tokens.accessToken(), tokens.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));

    }

    /** Регистрирует пользователя с ролью по умолчанию. */

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody @Valid RegistrationDTO registrationDTO) {
        EmployeeDTO employee = EmployeeDtoMapper.toDto(authService.registration(
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getEmail(),
                registrationDTO.getPassword(),
                registrationDTO.getConfirmPassword()
        ));
        ApiResponse<EmployeeDTO> response = ApiResponse.success("Registration successful", employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    /** Обновляет пару JWT-токенов по refresh token из заголовка запроса. */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(value = "Refresh-Token") String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        AuthTokens tokens = authService.refresh(refreshToken);
        JwtResponse response = new JwtResponse(tokens.accessToken(), tokens.refreshToken());
        ApiResponse<JwtResponse> body = ApiResponse.success("Token refreshed", response);
        return ResponseEntity.ok(body);

    }

    /** Завершает клиентскую сессию, удаляя access- и refresh-cookie. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", "jwtAccess=; Max-Age=0; Path=/; HttpOnly");
        response.addHeader("Set-Cookie", "jwtRefresh=; Max-Age=0; Path=/; HttpOnly");
        ApiResponse<Void> responseBody = ApiResponse.success("Logout successful");

        return ResponseEntity.ok(responseBody);
    }
}
