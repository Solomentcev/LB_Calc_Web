package com.lb_calc_web.security;

import com.lb_calc_web.domain.attributes.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Представление сотрудника для Spring Security.
 *
 * <p>Содержит только данные, необходимые для аутентификации
 * и авторизации пользователя.</p>
 */
public final class EmployeePrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public EmployeePrincipal(
            Long id,
            String email,
            String passwordHash,
            Role role
    ) {
        this.id = Objects.requireNonNull(id, "ID сотрудника не должен быть null");
        this.email = Objects.requireNonNull(email, "Email не должен быть null");
        this.passwordHash = Objects.requireNonNull(
                passwordHash,
                "Хэш пароля не должен быть null"
        );
        this.role = Objects.requireNonNull(
                role,
                "Роль не должна быть null"
        );
    }

    /**
     * Идентификатор сотрудника.
     */
    public Long getId() {
        return id;
    }

    /**
     * Email сотрудника.
     *
     * <p>Используется как username в Spring Security.</p>
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Хэш пароля.
     */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /**
     * Роль сотрудника.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Полномочия пользователя.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role.name())
        );
    }

    /**
     * Учетная запись не просрочена.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Учетная запись не заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Учетные данные не просрочены.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Пользователь активен.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "EmployeePrincipal{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
