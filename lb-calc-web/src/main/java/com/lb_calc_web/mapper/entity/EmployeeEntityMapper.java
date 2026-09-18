package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;

/**
 * Маппер между JPA-сущностью сотрудника и доменной моделью.
 *
 * <p>Маппинг не переносит технические данные persistence/security-слоя:
 * идентификатор и хэш пароля остаются в {@link EmployeeEntity}.</p>
 */
public final class EmployeeEntityMapper {

    public EmployeeEntityMapper() {
    }

    /**
     * Преобразует JPA-сущность в доменную модель.
     *
     * @param entity JPA-сущность сотрудника
     * @return доменный сотрудник
     */
    public static Employee toDomain(EmployeeEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Employee(
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
     * <p>Идентификатор и passwordHash здесь намеренно не заполняются.
     * Они относятся к persistence/security-слою.</p>
     *
     * @param domain доменный сотрудник
     * @return новая JPA-сущность
     */
    public static EmployeeEntity toEntity(Employee domain) {
        if (domain == null) {
            return null;
        }

        EmployeeEntity entity = new EmployeeEntity();

        updateEntity(domain, entity);

        return entity;
    }

    /**
     * Обновляет существующую JPA-сущность данными доменной модели.
     *
     * <p>Идентификатор и passwordHash существующей сущности сохраняются.</p>
     *
     * @param domain доменная модель
     * @param entity существующая JPA-сущность
     */
    public static void updateEntity(
            Employee domain,
            EmployeeEntity entity
    ) {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "Доменный сотрудник не должен быть null"
            );
        }

        if (entity == null) {
            throw new IllegalArgumentException(
                    "EmployeeEntity не должен быть null"
            );
        }

        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setRegistrationDate(domain.getRegistrationDate());
        entity.setRole(domain.getRole());
    }
}
