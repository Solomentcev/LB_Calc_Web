package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeEntityMapperTest {

    @Test
    void toEntity_shouldMapDomainFields() {
        Employee domain =
                new Employee(
                        "Иван",
                        "Иванов",
                        "ivanov@example.com",
                        LocalDate.of(2026, 9, 18),
                        Role.ROLE_MANAGER
                );

        EmployeeEntity entity =
                EmployeeEntityMapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals("Иван", entity.getFirstName());
        assertEquals("Иванов", entity.getLastName());
        assertEquals("ivanov@example.com", entity.getEmail());
        assertEquals(
                LocalDate.of(2026, 9, 18),
                entity.getRegistrationDate()
        );
        assertEquals(Role.ROLE_MANAGER, entity.getRole());
        assertNull(entity.getEncryptedPassword());
    }

    @Test
    void toDomain_shouldIgnorePersistencePassword() {
        EmployeeEntity entity = new EmployeeEntity();

        entity.setFirstName("Петр");
        entity.setLastName("Петров");
        entity.setEmail("petrov@example.com");
        entity.setEncryptedPassword("HASH");
        entity.setRegistrationDate(
                LocalDate.of(2026, 9, 18)
        );
        entity.setRole(Role.ROLE_ADMIN);

        Employee domain =
                EmployeeEntityMapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("Петр", domain.getFirstName());
        assertEquals("Петров", domain.getLastName());
        assertEquals(
                "petrov@example.com",
                domain.getEmail()
        );
        assertEquals(Role.ROLE_ADMIN, domain.getRole());
    }

    @Test
    void updateEntity_shouldKeepExistingPassword() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setEncryptedPassword("EXISTING_HASH");

        Employee domain =
                new Employee(
                        "НовоеИмя",
                        "НоваяФамилия",
                        "new@example.com",
                        LocalDate.of(2026, 9, 18),
                        Role.ROLE_ADMIN
                );

        EmployeeEntityMapper.updateEntity(
                domain,
                entity
        );

        assertEquals(
                "EXISTING_HASH",
                entity.getEncryptedPassword()
        );
        assertEquals(
                "НовоеИмя",
                entity.getFirstName()
        );
        assertEquals(
                "НоваяФамилия",
                entity.getLastName()
        );
        assertEquals(
                "new@example.com",
                entity.getEmail()
        );
    }

    @Test
    void nullArguments_shouldBeHandledAsDefinedByMapperContract() {
        assertNull(
                EmployeeEntityMapper.toEntity(null)
        );

        assertNull(
                EmployeeEntityMapper.toDomain(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmployeeEntityMapper.updateEntity(
                        null,
                        new EmployeeEntity()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> EmployeeEntityMapper.updateEntity(
                        new Employee(
                                "Иван",
                                "Иванов",
                                "ivanov@example.com",
                                LocalDate.of(2026, 9, 18),
                                Role.ROLE_MANAGER
                        ),
                        null
                )
        );
    }
}
