package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Репозиторий для работы с сотрудниками.
 */
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    /**
     * Находит сотрудника по email.
     *
     * @param email email сотрудника
     * @return найденный сотрудник или {@link Optional#empty()}
     */
    Optional<EmployeeEntity> findByEmail(String email);

    /**
     * Проверяет существование сотрудника с указанным email.
     *
     * @param email email сотрудника
     * @return {@code true}, если сотрудник существует
     */
    boolean existsByEmail(String email);
}