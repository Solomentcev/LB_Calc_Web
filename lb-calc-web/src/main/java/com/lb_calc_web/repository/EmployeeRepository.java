package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Репозиторий для работы с сотрудниками.
 */
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long>, JpaSpecificationExecutor<EmployeeEntity> {

    Page<EmployeeEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email, Pageable pageable);

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