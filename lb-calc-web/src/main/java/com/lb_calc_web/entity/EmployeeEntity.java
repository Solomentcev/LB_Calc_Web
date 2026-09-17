package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.Role;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * JPA-сущность сотрудника системы.
 *
 * <p>Содержит персональные данные сотрудника, его роль
 * и хэш пароля для аутентификации.</p>
 *
 * <p>Исходный пароль в базе не хранится.
 * Поле {@code passwordHash} содержит результат работы
 * {@code PasswordEncoder}.</p>
 */
@Entity
@Table(
        name = "employee",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_email",
                        columnNames = "email"
                )
        }
)
public class EmployeeEntity {

    /**
     * Идентификатор сотрудника.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя сотрудника.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Фамилия сотрудника.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Электронная почта.
     *
     * <p>Используется как уникальный идентификатор
     * сотрудника при аутентификации в текущей системе.</p>
     */
    @Column(nullable = false)
    private String email;

    /**
     * Хэш пароля сотрудника.
     *
     * <p>Исходный пароль здесь не хранится.</p>
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Дата регистрации сотрудника.
     */
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    /**
     * Роль сотрудника.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Конструктор для JPA.
     */
    protected EmployeeEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = Objects.requireNonNull(
                firstName,
                "Имя сотрудника не должно быть null"
        );
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = Objects.requireNonNull(
                lastName,
                "Фамилия сотрудника не должна быть null"
        );
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(
                email,
                "Email сотрудника не должен быть null"
        );
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(
                passwordHash,
                "Хэш пароля не должен быть null"
        );
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = Objects.requireNonNull(
                registrationDate,
                "Дата регистрации не должна быть null"
        );
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = Objects.requireNonNull(
                role,
                "Роль сотрудника не должна быть null"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EmployeeEntity that = (EmployeeEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='[PROTECTED]'" +
                ", registrationDate=" + registrationDate +
                ", role=" + role +
                '}';
    }
}