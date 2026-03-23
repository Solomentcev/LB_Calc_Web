package com.lb_calc_web.dto;

import com.lb_calc_web.model.user.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class EmployeeDTO implements UserDetails  {
    private Long id;
    @NotBlank
    private String firstName;
    @NotBlank
    @Size(max = 20)
    private String lastName;
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    @NotBlank
    @Size(max = 120)
    private String password;
    private String encryptedPassword;
    private LocalDate registrationDate;
    @Enumerated(EnumType.STRING)
    private Role role;

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public EmployeeDTO() {
    }


    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank @Size(max = 20) String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank @Size(max = 20) String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank @Size(max = 50) @Email String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Size(max = 50) @Email String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public @NotBlank @Size(max = 120) String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return lastName+" "+firstName;
    }

    public void setPassword(@NotBlank @Size(max = 120) String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "EmployeeDTO{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", registrationDate=" + registrationDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeDTO employeeDTO = (EmployeeDTO) o;
        return Objects.equals(getFirstName(), employeeDTO.getFirstName()) && Objects.equals(getLastName(), employeeDTO.getLastName()) && Objects.equals(getEmail(), employeeDTO.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getEmail());
    }
}
