package com.lb_calc_web.service;

import com.lb_calc_web.security.jwt.JwtService;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import static org.mockito.ArgumentMatchers.eq;
import java.util.concurrent.CompletableFuture;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.event.UserEventType;
import com.lb_calc_web.event.UserEvent;
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

    @Test
    void publishEvent_shouldWaitForSuccessfulTransactionCommit() {
        KafkaTemplate<String, UserEvent> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(eq("user-events"), eq("42"), any(UserEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        AuthService authService = new AuthService(
                mock(EmployeeService.class),
                mock(EmployeeRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                kafkaTemplate,
                "user-events"
        );

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            authService.publishEvent(UserEventType.USER_REGISTERED, 42L, "user@example.com");
            verifyNoInteractions(kafkaTemplate);

            TransactionSynchronizationUtils.invokeAfterCommit(
                    TransactionSynchronizationManager.getSynchronizations()
            );

            verify(kafkaTemplate).send(eq("user-events"), eq("42"), any(UserEvent.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void publishEvent_shouldNotSendWhenTransactionRollsBack() {
        KafkaTemplate<String, UserEvent> kafkaTemplate = mock(KafkaTemplate.class);
        AuthService authService = new AuthService(
                mock(EmployeeService.class),
                mock(EmployeeRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                kafkaTemplate,
                "user-events"
        );

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            authService.publishEvent(UserEventType.USER_REGISTERED, 42L, "user@example.com");
            TransactionSynchronizationUtils.invokeAfterCompletion(
                    TransactionSynchronizationManager.getSynchronizations(),
                    org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK
            );
            verifyNoInteractions(kafkaTemplate);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }


    @Test
    void registration_shouldRejectMissingPasswordBeforeDatabaseAccess() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

        AuthService authService = new AuthService(
                mock(EmployeeService.class),
                employeeRepository,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(KafkaTemplate.class),
                "user-events"
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authService.registration("Иван", "Иванов", "user@example.com", null, null)
        );

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void changePassword_shouldRejectBlankPasswordBeforeDatabaseAccess() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        AuthService authService = new AuthService(
                mock(EmployeeService.class),
                employeeRepository,
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                mock(KafkaTemplate.class),
                "user-events"
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(1L, " ")
        );

        verifyNoInteractions(employeeRepository);
    }


    @Test
    void registration_shouldTranslateUniqueEmailConstraintViolation() {
        EmployeeService employeeService = mock(EmployeeService.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        KafkaTemplate<String, UserEvent> kafkaTemplate = mock(KafkaTemplate.class);

        when(employeeService.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        ConstraintViolationException violation = new ConstraintViolationException(
                "duplicate email", new java.sql.SQLException("duplicate"), "uk_employee_email");
        when(employeeRepository.save(any(EmployeeEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email", violation));

        AuthService authService = new AuthService(
                employeeService,
                employeeRepository,
                passwordEncoder,
                mock(JwtService.class),
                kafkaTemplate,
                "user-events"
        );

        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> authService.registration(
                    "Иван", "Иванов", "user@example.com", "secret", "secret")
        );

        org.junit.jupiter.api.Assertions.assertEquals("Email уже используется", exception.getMessage());
        verifyNoInteractions(kafkaTemplate);
    }


    @Test
    void publishEvent_shouldSwallowSynchronousKafkaFailure() {
        KafkaTemplate<String, UserEvent> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(eq("user-events"), eq("42"), any(UserEvent.class)))
                .thenThrow(new IllegalStateException("Kafka недоступен"));

        AuthService authService = new AuthService(
                mock(EmployeeService.class),
                mock(EmployeeRepository.class),
                mock(PasswordEncoder.class),
                mock(JwtService.class),
                kafkaTemplate,
                "user-events"
        );

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> authService.publishEvent(
                        UserEventType.USER_REGISTERED, 42L, "user@example.com")
        );

        verify(kafkaTemplate).send(eq("user-events"), eq("42"), any(UserEvent.class));
    }

    @Test
    void createEmployee_shouldPropagateNonEmailConstraintViolation() {
        EmployeeService employeeService = mock(EmployeeService.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        when(employeeService.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        DataIntegrityViolationException violation = new DataIntegrityViolationException(
                "foreign key violation",
                new ConstraintViolationException(
                        "foreign key violation",
                        new java.sql.SQLException("foreign key violation"),
                        "fk_employee_role"
                )
        );
        when(employeeRepository.save(any(EmployeeEntity.class))).thenThrow(violation);

        AuthService authService = new AuthService(
                employeeService,
                employeeRepository,
                passwordEncoder,
                mock(JwtService.class),
                mock(KafkaTemplate.class),
                "user-events"
        );

        DataIntegrityViolationException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> authService.createEmployee(
                    new Employee("Иван", "Иванов", "user@example.com",
                            java.time.LocalDate.now(), Role.ROLE_MANAGER),
                    "secret",
                    "secret")
        );

        org.junit.jupiter.api.Assertions.assertSame(violation, thrown);
    }

}
