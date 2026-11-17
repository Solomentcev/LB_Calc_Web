package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Role;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Доменная модель сотрудника.
 *
 * <p>Идентичность сохранённого сотрудника определяется стабильным
 * идентификатором. Email является изменяемым атрибутом и не используется
 * для идентификации сотрудника.</p>
 *
 * <p>Для нового сотрудника идентификатор может отсутствовать до сохранения.</p>
 */
public class Employee {

    private final Long id;
    private String firstName;
    private String lastName;
    private String email;
    private final LocalDate registrationDate;
    private Role role;

    /**
     * Создаёт нового сотрудника, ещё не имеющего persistence-идентификатора.
     */
    public Employee(
            String firstName,
            String lastName,
            String email,
            LocalDate registrationDate,
            Role role
    ) {
        this(
                null,
                firstName,
                lastName,
                email,
                registrationDate,
                role
        );
    }

    /**
     * Восстанавливает сотрудника с существующим идентификатором.
     *
     * @param id идентификатор сохранённого сотрудника; для нового сотрудника null
     * @param firstName имя
     * @param lastName фамилия
     * @param email адрес электронной почты
     * @param registrationDate дата регистрации
     * @param role роль
     */
    public Employee(
            Long id,
            String firstName,
            String lastName,
            String email,
            LocalDate registrationDate,
            Role role
    ) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException(
                    "ID сотрудника должен быть положительным числом"
            );
        }

        this.id = id;
        this.firstName = Objects.requireNonNull(
                firstName,
                "Имя сотрудника не должно быть null"
        );
        this.lastName = Objects.requireNonNull(
                lastName,
                "Фамилия сотрудника не должна быть null"
        );
        this.email = Objects.requireNonNull(
                email,
                "Email сотрудника не должен быть null"
        );
        this.registrationDate = Objects.requireNonNull(
                registrationDate,
                "Дата регистрации не должна быть null"
        );
        this.role = Objects.requireNonNull(
                role,
                "Роль сотрудника не должна быть null"
        );
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public Role getRole() {
        return role;
    }

    /**
     * Изменяет основные персональные данные сотрудника.
     */
    public void changePersonalData(
            String firstName,
            String lastName,
            String email
    ) {
        this.firstName = Objects.requireNonNull(
                firstName,
                "Имя сотрудника не должно быть null"
        );
        this.lastName = Objects.requireNonNull(
                lastName,
                "Фамилия сотрудника не должна быть null"
        );
        this.email = Objects.requireNonNull(
                email,
                "Email сотрудника не должен быть null"
        );
    }

    /**
     * Изменяет роль сотрудника.
     */
    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(
                role,
                "Роль сотрудника не должна быть null"
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Employee employee = (Employee) object;

        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
