package com.lb_calc_web.service;

import com.lb_calc_web.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void refresh_shouldRejectTokenWithUnexpectedType() {
        EmployeeService employeeService = mock(EmployeeService.class);
        JwtService jwtService = mock(JwtService.class);
        Claims claims = mock(Claims.class);

        AuthService authService = new AuthService(
                employeeService,
                mock(com.lb_calc_web.repository.EmployeeRepository.class),
                mock(PasswordEncoder.class),
                jwtService,
                mock(KafkaTemplate.class),
                "user-events"
        );

        when(jwtService.getRefreshClaims("signed-token")).thenReturn(claims);
        when(jwtService.isRefresh(claims)).thenReturn(false);

        assertThrows(
                JwtException.class,
                () -> authService.refresh("signed-token")
        );

        verifyNoInteractions(employeeService);
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }
}
