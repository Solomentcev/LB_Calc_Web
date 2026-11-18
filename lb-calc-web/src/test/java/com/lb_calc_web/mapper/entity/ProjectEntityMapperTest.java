package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProjectEntityMapperTest {

    @Test
    void roundTrip_shouldPreserveProjectFieldsAndAggregatedAlsQuantity() {
        LocalDate createdAt = LocalDate.of(2026, 10, 1);
        Employee employee = new Employee(
                "Иван",
                "Иванов",
                "ivanov@example.com",
                createdAt,
                Role.ROLE_MANAGER
        );

        ALS als = TestDataFactory.validALS(null);
        Project domain = new Project(
                "Проект",
                "Компания",
                createdAt,
                employee
        );
        domain.addALS(als);
        domain.addALS(als);

        EmployeeEntity employeeEntity = EmployeeEntityMapper.toEntity(employee);
        employeeEntity.setEncryptedPassword("HASH");
        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                als,
                ModuleEntityMapper::toEntity
        );

        ProjectEntity entity = ProjectEntityMapper.toEntity(
                domain,
                ignored -> employeeEntity,
                ignored -> alsEntity
        );

        assertEquals(1, entity.getAlsEntries().size());
        assertEquals(2, entity.getAlsEntries().get(0).getQuantity());
        assertSame(entity, entity.getAlsEntries().get(0).getProject());
        assertSame(alsEntity, entity.getAlsEntries().get(0).getAls());

        AtomicReference<ALS> mappedAls = new AtomicReference<>();
        AtomicReference<ALSEntity> mappedAlsEntity = new AtomicReference<>();

        Project restored = ProjectEntityMapper.toDomain(
                entity,
                (mappedDomain, mappedEntity) -> {
                    mappedAls.set(mappedDomain);
                    mappedAlsEntity.set(mappedEntity);
                }
        );

        assertEquals(als, mappedAls.get());
        assertSame(alsEntity, mappedAlsEntity.get());
        assertEquals(domain.getName(), restored.getName());
        assertEquals(domain.getCompany(), restored.getCompany());
        assertEquals(domain.getCreatedAt(), restored.getCreatedAt());
        assertEquals(domain.getUpdatedAt(), restored.getUpdatedAt());
        assertEquals(2, restored.getQuantity(als));
        assertEquals(domain.getDescription(), restored.getDescription());
    }

    @Test
    void toDomain_shouldRejectStaleDerivedDescription() {
        LocalDate createdAt = LocalDate.of(2026, 10, 1);
        Employee employee = new Employee(
                "Иван",
                "Иванов",
                "ivanov@example.com",
                createdAt,
                Role.ROLE_MANAGER
        );
        Project domain = new Project("Проект", "Компания", createdAt, employee);
        EmployeeEntity employeeEntity = EmployeeEntityMapper.toEntity(employee);
        employeeEntity.setEncryptedPassword("HASH");
        ProjectEntity entity = ProjectEntityMapper.toEntity(
                domain,
                ignored -> employeeEntity,
                ignored -> {
                    throw new AssertionError("ALS не ожидается");
                }
        );
        entity.setDescription("Устаревшее описание");

        assertThrows(
                IllegalStateException.class,
                () -> ProjectEntityMapper.toDomain(entity)
        );
    }

    @Test
    void updateEntity_shouldReuseExistingProjectAlsAssociation() {
        LocalDate createdAt = LocalDate.of(2026, 10, 1);
        Employee employee = new Employee(
                "Иван",
                "Иванов",
                "ivanov@example.com",
                createdAt,
                Role.ROLE_MANAGER
        );
        EmployeeEntity employeeEntity = EmployeeEntityMapper.toEntity(employee);
        employeeEntity.setEncryptedPassword("HASH");

        ALS als = TestDataFactory.validALS(null);
        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                als,
                ModuleEntityMapper::toEntity
        );

        Project initial = new Project("Проект", "Компания", createdAt, employee);
        initial.addALS(als);

        ProjectEntity entity = ProjectEntityMapper.toEntity(
                initial,
                ignored -> employeeEntity,
                ignored -> alsEntity
        );

        var association = entity.getAlsEntries().get(0);

        Project changed = Project.restore(
                "Обновлённый проект",
                "Компания",
                createdAt,
                employee,
                LocalDate.of(2026, 10, 2),
                employee,
                Map.of(als, 3)
        );

        ProjectEntityMapper.updateEntity(
                changed,
                entity,
                ignored -> employeeEntity,
                ignored -> alsEntity
        );

        assertEquals(1, entity.getAlsEntries().size());
        assertSame(association, entity.getAlsEntries().get(0));
        assertEquals(3, association.getQuantity());
    }
}
