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
                entity.getRole(),
                entity.isActive()
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

        return EmployeeEntityMapper.toDto(entity);
    }

    public EmployeeDTO loadUserById(int id) {
        return loadUserById((long) id);
    }

    /**
     * Загружает сотрудника по стабильному идентификатору.
     */
    public EmployeeDTO loadUserById(Long id) {
        EmployeeEntity entity = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь с id=" + id + " не найден"
                        )
                );

        return EmployeeEntityMapper.toDto(entity);
    }

    public ProfileDTO getProfileById(int id) {

        EmployeeEntity entity = employeeRepository
                .findById((long) id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь с id=" + id + " не найден"
                        )
                );

        return EmployeeEntityMapper.toProfileDto(entity);
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

        return EmployeeEntityMapper.toDto(saved);
    }

    /**
     * Деактивирует учётную запись без удаления сотрудника из БД.
     *
     * <p>Запись сохраняется, чтобы не нарушать ссылки проектов
     * на создателя и последнего редактора.</p>
     *
     * @param id идентификатор сотрудника
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public void deactivateById(Long id) {
        EmployeeEntity entity = getRequiredEntity(id);

        if (entity.isActive()) {
            entity.setActive(false);
            employeeRepository.save(entity);
        }

        logger.info("Учётная запись сотрудника деактивирована: id={}", id);
    }

    /**
     * Повторно активирует ранее деактивированную учётную запись.
     *
     * @param id идентификатор сотрудника
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional
    public void activateById(Long id) {
        EmployeeEntity entity = getRequiredEntity(id);

        if (!entity.isActive()) {
            entity.setActive(true);
            employeeRepository.save(entity);
        }

        logger.info("Учётная запись сотрудника активирована: id={}", id);
    }

    /**
     * Возвращает состояние активности учётной записи.
     *
     * @param id идентификатор сотрудника
     * @return {@code true}, если учётная запись активна
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public boolean isActive(Long id) {
        return getRequiredEntity(id).isActive();
    }

    /**
     * Возвращает состояния активности сотрудников по их ID.
     *
     * @return соответствие ID сотрудника признаку активности
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public java.util.Map<Long, Boolean> getActiveStatuses() {
        return employeeRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        EmployeeEntity::getId,
                        EmployeeEntity::isActive
                ));
    }

    private EmployeeEntity getRequiredEntity(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Сотрудник с id=" + id + " не найден"
                        )
                );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<EmployeeDTO> findAll() {

        List<EmployeeEntity> entities =
                employeeRepository.findAll();

        entities.sort(
                Comparator.comparing(EmployeeEntity::getId)
        );

        return entities.stream()
                .map(EmployeeEntityMapper::toDto)
                .toList();
    }

    public List<ProfileDTO> getAllProfiles() {

        List<EmployeeEntity> entities =
                employeeRepository.findAll();

        entities.sort(
                Comparator.comparing(EmployeeEntity::getId)
        );

        return entities.stream()
                .map(EmployeeEntityMapper::toProfileDto)
                .toList();
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
            return loadUserById(employeePrincipal.getId());
        }

        return loadUserByEmail(
                authentication.getName()
        );
    }


}