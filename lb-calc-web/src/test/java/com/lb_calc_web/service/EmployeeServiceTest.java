package com.lb_calc_web.service;

import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Проверки управления активностью учётных записей сотрудников.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeEntity employee;

    @BeforeEach
    void setUp() {
        employee = new EmployeeEntity();
        employee.setActive(true);
    }

    @Test
    void deactivateById_shouldDeactivateWithoutDeletingEmployee() {
        when(employeeRepository.findById(15L))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee))
                .thenReturn(employee);

        employeeService.deactivateById(15L);

        assertFalse(employee.isActive());
        verify(employeeRepository).save(employee);
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void deactivateById_shouldBeIdempotent() {
        employee.setActive(false);
        when(employeeRepository.findById(15L))
                .thenReturn(Optional.of(employee));

        employeeService.deactivateById(15L);

        assertFalse(employee.isActive());
        verify(employeeRepository, never()).save(any());
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void activateById_shouldReactivateEmployee() {
        employee.setActive(false);
        when(employeeRepository.findById(15L))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee))
                .thenReturn(employee);

        employeeService.activateById(15L);

        assertTrue(employee.isActive());
        verify(employeeRepository).save(employee);
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void deactivateById_shouldFailWhenEmployeeDoesNotExist() {
        when(employeeRepository.findById(15L))
                .thenReturn(Optional.empty());

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> employeeService.deactivateById(15L)
        );

        verify(employeeRepository, never()).save(any());
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCurrentEmployee_shouldRejectAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "test-key",
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        try {
            assertThrows(
                    java.util.NoSuchElementException.class,
                    () -> employeeService.getCurrentEmployee()
            );
            verifyNoInteractions(employeeRepository);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

}
