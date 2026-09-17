package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityMapperTest {

    @Test
    void shouldMapDomainToEntity() {
        LocalDate registrationDate = LocalDate.of(2026, 9, 18);

        Employee domain = new Employee(
                "Иван",
                "Иванов",
                "ivanov@example.com",
                registrationDate,
                Role.ROLE_MANAGER
        );

        EmployeeEntity entity = EmployeeEntityMapper.toEntity(domain);

        assertNotNull(entity);

        assertNull(entity.getId());
        assertEquals("Иван", entity.getFirstName());
        assertEquals("Иванов", entity.getLastName());
        assertEquals("ivanov@example.com", entity.getEmail());
        assertEquals(registrationDate, entity.getRegistrationDate());
        assertEquals(Role.ROLE_MANAGER, entity.getRole());

        // Пароль Domain -> Entity mapper не устанавливает
        assertNull(entity.getPasswordHash());
    }

    @Test
    void shouldMapEntityToDomain() {
        LocalDate registrationDate = LocalDate.of(2026, 9, 18);

        EmployeeEntity entity = new EmployeeEntity();

        entity.setFirstName("Петр");
        entity.setLastName("Петров");
        entity.setEmail("petrov@example.com");
        entity.setPasswordHash("HASH");
        entity.setRegistrationDate(registrationDate);
        entity.setRole(Role.ROLE_ADMIN);

        Employee domain = EmployeeEntityMapper.toDomain(entity);

        assertNotNull(domain);

        assertEquals("Петр", domain.getFirstName());
        assertEquals("Петров", domain.getLastName());
        assertEquals("petrov@example.com", domain.getEmail());
        assertEquals(registrationDate, domain.getRegistrationDate());
        assertEquals(Role.ROLE_ADMIN, domain.getRole());
    }

    @Test
    void shouldPreservePasswordHashWhenUpdatingEntity() {
        EmployeeEntity entity = new EmployeeEntity();

        entity.setFirstName("СтароеИмя");
        entity.setLastName("СтараяФамилия");
        entity.setEmail("old@example.com");
        entity.setPasswordHash("EXISTING_HASH");
        entity.setRegistrationDate(
                LocalDate.of(2025, 1, 1)
        );
        entity.setRole(Role.ROLE_MANAGER);

        Employee domain = new Employee(
                "НовоеИмя",
                "НоваяФамилия",
                "new@example.com",
                LocalDate.of(2026, 9, 18),
                Role.ROLE_ADMIN
        );

        EmployeeEntityMapper.updateEntity(domain, entity);

        assertEquals("НовоеИмя", entity.getFirstName());
        assertEquals("НоваяФамилия", entity.getLastName());
        assertEquals("new@example.com", entity.getEmail());
        assertEquals(
                LocalDate.of(2026, 9, 18),
                entity.getRegistrationDate()
        );
        assertEquals(Role.ROLE_ADMIN, entity.getRole());

        // mapper не должен затирать security-данные
        assertEquals(
                "EXISTING_HASH",
                entity.getPasswordHash()
        );
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(
                EmployeeEntityMapper.toEntity(null)
        );
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(
                EmployeeEntityMapper.toDomain(null)
        );
    }

    @Test
    void shouldRejectNullDomainWhenUpdatingEntity() {
        EmployeeEntity entity = new EmployeeEntity();

        assertThrows(
                IllegalArgumentException.class,
                () -> EmployeeEntityMapper.updateEntity(
                        null,
                        entity
                )
        );
    }

    @Test
    void shouldRejectNullEntityWhenUpdatingEntity() {
        Employee domain = new Employee(
                "Иван",
                "Иванов",
                "ivanov@example.com",
                LocalDate.of(2026, 9, 18),
                Role.ROLE_MANAGER
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmployeeEntityMapper.updateEntity(
                        domain,
                        null
                )
        );
    }
}