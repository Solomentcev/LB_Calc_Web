package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;

import java.util.Objects;

/**
 * Маппер между JPA-сущностью сотрудника и доменной моделью.
 *
 * <p>Идентификатор сотрудника переносится в доменную модель,
 * поскольку он является стабильной идентичностью сотрудника.
 * Хэш пароля остаётся исключительно в persistence/security-слое.</p>
 */
public final class EmployeeEntityMapper {

    private EmployeeEntityMapper() {
    }

    /**
     * Преобразует JPA-сущность в доменную модель.
     *
     * @param entity JPA-сущность сотрудника
     * @return доменный сотрудник или null, если передан null
     */
    public static Employee toDomain(EmployeeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Employee(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRegistrationDate(),
                entity.getRole()
        );
    }

    /**
     * Создаёт новую JPA-сущность из доменного объекта.
     *
     * <p>Идентификатор назначается базой данных, а хэш пароля
     * устанавливается отдельным security-сценарием.</p>
     *
     * @param domain доменный сотрудник
     * @return новая JPA-сущность или null, если передан null
     */
    public static EmployeeEntity toEntity(Employee domain) {
        if (domain == null) {
            return null;
        }

        if (domain.getId() != null) {
            throw new IllegalArgumentException(
                    "Нельзя создавать новую EmployeeEntity из уже сохранённого сотрудника"
            );
        }

        EmployeeEntity entity = new EmployeeEntity();
        updateEntity(domain, entity);

        return entity;
    }

    /**
     * Обновляет существующую JPA-сущность данными доменной модели.
     *
     * <p>Идентификатор и хэш пароля существующей сущности сохраняются.</p>
     *
     * @param domain доменная модель
     * @param entity существующая JPA-сущность
     */
    public static void updateEntity(
            Employee domain,
            EmployeeEntity entity
    ) {
        Objects.requireNonNull(
                domain,
                "Доменный сотрудник не должен быть null"
        );
        Objects.requireNonNull(
                entity,
                "EmployeeEntity не должен быть null"
        );

        if (domain.getId() != null
                && !domain.getId().equals(entity.getId())) {
            throw new IllegalArgumentException(
                    "ID доменного сотрудника не совпадает с ID EmployeeEntity"
            );
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        if (entity.getRegistrationDate() == null) {
            entity.setRegistrationDate(domain.getRegistrationDate());
        }
        entity.setRole(domain.getRole());
    }
}
