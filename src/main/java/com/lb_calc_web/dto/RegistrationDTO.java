package com.lb_calc_web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationDTO {
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
    @NotBlank
    @Size(max = 120)
    private String confirmPassword;

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

    public @NotBlank @Size(max = 120) String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank @Size(max = 120) String password) {
        this.password = password;
    }

    public @NotBlank @Size(max = 120) String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(@NotBlank @Size(max = 120) String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
