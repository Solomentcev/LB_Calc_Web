package com.lb_calc_web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {
    @NotBlank
    @Size(max = 120)
    private String password;
    @NotBlank
    @Size(max = 120)
    private String confirmPassword;

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
