package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Role;

import java.time.LocalDate;
import java.util.Objects;

public class Employee {

    private String firstName;
    private String lastName;
    private String email;
    private LocalDate registrationDate;
    private Role role;

    public Employee(
            String firstName,
            String lastName,
            String email,
            LocalDate registrationDate,
            Role role
    ) {
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.email = Objects.requireNonNull(email);
        this.registrationDate = Objects.requireNonNull(registrationDate);
        this.role = Objects.requireNonNull(role);
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

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role);
    }
}