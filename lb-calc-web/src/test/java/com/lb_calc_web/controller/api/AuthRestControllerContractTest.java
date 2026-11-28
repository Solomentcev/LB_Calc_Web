package com.lb_calc_web.controller.api;

import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import com.lb_calc_web.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет HTTP-контракт REST-операций аутентификации и регистрации. */
@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RestExceptionHandler.class)
class AuthRestControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void login_shouldRejectInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Некорректный запрос"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void login_shouldReturn401ForInvalidCredentials() throws Exception {
        when(authService.login("user@example.com", "wrong-password"))
                .thenThrow(new AuthException("Неверный пароль"));

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Неверные учётные данные"));
    }

    @Test
    void refresh_shouldReturn400WhenHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Некорректный запрос"));
    }

    @Test
    void refresh_shouldReturn400WhenHeaderIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/refresh")
                        .header("Refresh-Token", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Некорректный запрос"));
    }

    @Test
    void registration_shouldReturn400WhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Иван",
                                  "lastName": "Иванов",
                                  "email": "user@example.com",
                                  "password": "secret123",
                                  "confirmPassword": "different123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Некорректный запрос"));
    }

    @Test
    void refresh_shouldReturn401ForInvalidToken() throws Exception {
        when(authService.refresh("invalid-token"))
                .thenThrow(new JwtException("Подробности проверки токена"));

        mockMvc.perform(post("/api/v1/refresh")
                        .header("Refresh-Token", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Недействительный или просроченный токен"));
    }

    @Test
    void logout_shouldClearTokenCookies() throws Exception {
        mockMvc.perform(post("/api/v1/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void registration_shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(authService.registration(
                "Иван", "Иванов", "user@example.com", "secret123", "secret123"))
                .thenThrow(new IllegalStateException("Email уже используется"));

        mockMvc.perform(post("/api/v1/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Иван",
                                  "lastName": "Иванов",
                                  "email": "user@example.com",
                                  "password": "secret123",
                                  "confirmPassword": "secret123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message")
                        .value("Операцию невозможно выполнить из-за текущего состояния данных"));
    }
}
