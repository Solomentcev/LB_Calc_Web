package com.lb_calc_web.dto;

import com.lb_calc_web.model.user.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ProfileDTO {
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
    private LocalDate registrationDate;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank String firstName) {
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

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "ProfileDTO{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", registrationDate=" + registrationDate +
                ", role=" + role +
                '}';
    }
}
