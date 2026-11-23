package com.lb_calc_web.controller.api;

import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.service.EmployeeService;
import com.lb_calc_web.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет разграничение доступа к REST-операциям сотрудников. */
@WebMvcTest(EmployeeRestController.class)
@AutoConfigureMockMvc
@Import({
        RestExceptionHandler.class,
        EmployeeRestControllerSecurityTest.MethodSecurityConfiguration.class
})
class EmployeeRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void profiles_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/employees/profiles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void profiles_shouldBeAvailableToAuthenticatedUser() throws Exception {
        when(employeeService.getAllProfiles()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/employees/profiles"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void employeeStatuses_shouldRemainAdminOnly() throws Exception {
        mockMvc.perform(get("/api/v1/employees/statuses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void employeeAdministration_shouldRemainAdminOnly() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                    .build();
        }
    }
}
