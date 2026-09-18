package com.lb_calc_web.service;

import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.security.EmployeePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService implements UserDetailsService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository
    ) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Используется Spring Security.
     *
     * Здесь возвращается именно EmployeePrincipal,
     * а не EmployeeDTO.
     */
    @Override
    public EmployeePrincipal loadUserByUsername(String email) {

        logger.debug(
                "Загрузка principal по email={}",
                email
        );

        EmployeeEntity entity = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Пользователь " + email + " не найден"
                        )
                );

        return new EmployeePrincipal(
                entity.getId(),
                entity.getEmail(),
                entity.getEncryptedPassword(),
                entity.getRole()
        );
    }

    /**
     * Обычная загрузка сотрудника приложения.
     *
     * Возвращает DTO, а не Security Principal.
     */
    public EmployeeDTO loadUserByEmail(String email) {

        logger.debug(
                "Загрузка сотрудника по email={}",
                email
        );

        EmployeeEntity entity = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Пользователь " + email + " не найден"
                        )
                );

        return toDto(entity);
    }

    public EmployeeDTO loadUserById(int id) {

        EmployeeEntity entity = employeeRepository
                .findById((long) id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь с id=" + id + " не найден"
                        )
                );

        return toDto(entity);
    }

    public ProfileDTO getProfileById(int id) {

        EmployeeEntity entity = employeeRepository
                .findById((long) id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь с id=" + id + " не найден"
                        )
                );

        Employee domain = EmployeeEntityMapper.toDomain(entity);

        ProfileDTO dto = EmployeeDtoMapper.toProfileDto(domain);
        dto.setId(entity.getId());

        return dto;
    }

    /**
     * Обновление существующего сотрудника.
     *
     * Пароль здесь не изменяется.
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public EmployeeDTO save(EmployeeDTO employeeDTO) {

        if (employeeDTO.getId() == null) {
            throw new IllegalArgumentException(
                    "Для обновления сотрудника ID не должен быть null"
            );
        }

        logger.info(
                "Обновление сотрудника id={}, email={}",
                employeeDTO.getId(),
                employeeDTO.getEmail()
        );

        EmployeeEntity entity = employeeRepository
                .findById(employeeDTO.getId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Сотрудник с id=" +
                                        employeeDTO.getId() +
                                        " не найден"
                        )
                );

        Employee domain =
                EmployeeDtoMapper.toDomain(employeeDTO);

        EmployeeEntityMapper.updateEntity(
                domain,
                entity
        );

        EmployeeEntity saved =
                employeeRepository.save(entity);

        return toDto(saved);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteById(Long id) {

        if (!employeeRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Сотрудник с id=" + id + " не найден"
            );
        }

        logger.info(
                "Удаление сотрудника id={}",
                id
        );

        employeeRepository.deleteById(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<EmployeeDTO> findAll() {

        List<EmployeeEntity> entities =
                employeeRepository.findAll();

        entities.sort(
                Comparator.comparing(EmployeeEntity::getId)
        );

        List<EmployeeDTO> result =
                new ArrayList<>();

        for (EmployeeEntity entity : entities) {
            result.add(toDto(entity));
        }

        return result;
    }

    public List<ProfileDTO> getAllProfiles() {

        List<EmployeeEntity> entities =
                employeeRepository.findAll();

        entities.sort(
                Comparator.comparing(EmployeeEntity::getId)
        );

        List<ProfileDTO> result =
                new ArrayList<>();

        for (EmployeeEntity entity : entities) {

            Employee domain =
                    EmployeeEntityMapper.toDomain(entity);

            ProfileDTO dto =
                    EmployeeDtoMapper.toProfileDto(domain);

            dto.setId(entity.getId());

            result.add(dto);
        }

        return result;
    }

    public boolean existsByEmail(String email) {

        return employeeRepository.existsByEmail(email);
    }

    /**
     * Возвращает DTO текущего сотрудника.
     *
     * В SecurityContext при этом находится EmployeePrincipal.
     */
    public EmployeeDTO getCurrentEmployee() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new NoSuchElementException(
                    "Пользователь не аутентифицирован"
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof EmployeePrincipal employeePrincipal) {

            return loadUserByEmail(
                    employeePrincipal.getUsername()
            );
        }

        return loadUserByEmail(
                authentication.getName()
        );
    }

    private EmployeeDTO toDto(EmployeeEntity entity) {

        Employee domain =
                EmployeeEntityMapper.toDomain(entity);

        EmployeeDTO dto =
                EmployeeDtoMapper.toDto(domain);

        dto.setId(entity.getId());

        return dto;
    }
}