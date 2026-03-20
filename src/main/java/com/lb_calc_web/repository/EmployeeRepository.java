package com.lb_calc_web.repository;

import com.lb_calc_web.model.user.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(@NotBlank @Size(max = 50) @Email String email);
}
