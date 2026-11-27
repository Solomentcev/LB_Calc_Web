package com.lb_calc_web.controller.api;

import com.lb_calc_web.handler.RestExceptionHandler;
import com.lb_calc_web.security.jwt.JwtService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.EmployeeService;
import com.lb_calc_web.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Проверяет административное разграничение доступа к каталогу оборудования. */
@WebMvcTest(EquipmentRestController.class)
@AutoConfigureMockMvc
@Import({
        RestExceptionHandler.class,
        EquipmentRestControllerSecurityTest.MethodSecurityConfiguration.class
})
class EquipmentRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentCatalogService equipmentCatalogService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private AuthService authService;

    @Test
    void catalog_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void catalog_shouldBeForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void catalog_shouldBeAvailableToAdministrator() throws Exception {
        when(equipmentCatalogService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/equipment"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void equipmentId_shouldBePositive() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void create_shouldRejectInvalidEquipmentAndReturnApiErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "functionalType": null,
                                  "purpose": "",
                                  "category": null,
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Некорректный запрос"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void unknownApiRoute_shouldReturn404ApiErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/unknown-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Запрашиваемый ресурс не найден"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void unsupportedMethod_shouldReturn405ApiErrorEnvelope() throws Exception {
        mockMvc.perform(put("/api/v1/equipment"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("HTTP-метод не поддерживается"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void unsupportedMediaType_shouldReturn415ApiErrorEnvelope() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<equipment/>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Формат содержимого запроса не поддерживается"));
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
