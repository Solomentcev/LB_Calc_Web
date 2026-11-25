package com.lb_calc_web.service;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.dto.ProfileDTO;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.security.EmployeePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public EmployeeDTO loadUserById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("Пользователь с id=" + id + " не найден");
        }
        EmployeeEntity entity = employeeRepository
                .findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Пользователь с id=" + id + " не найден"
                        )
                );

        return EmployeeEntityMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public ProfileDTO getProfileById(int id) {
        if (id <= 0) {
            throw new NoSuchElementException("Пользователь с id=" + id + " не найден");
        }

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

        if (employeeDTO == null || employeeDTO.getId() == null || employeeDTO.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Для обновления сотрудника требуется корректный ID"
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

        EmployeeEntityMapper.updateEntity(employeeDTO, entity);

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

    /**
     * Возвращает страницу сотрудников или профилей с фильтрами по столбцам.
     * Фильтрация выполняется в БД до сортировки и пагинации.
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProfileDTO> findProfilesPage(
            EmployeeCatalogFilter filter, org.springframework.data.domain.Pageable pageable
    ) {
        List<Specification<EmployeeEntity>> specifications = new ArrayList<>();
        if (filter.id() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("id"), filter.id()));
        }
        addTextFilter(specifications, "firstName", filter.firstName());
        addTextFilter(specifications, "lastName", filter.lastName());
        addTextFilter(specifications, "email", filter.email());

        if (filter.role() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("role"), filter.role()));
        }
        if (filter.active() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("active"), filter.active()));
        }
        if (filter.registrationDateFrom() != null) {
            specifications.add((root, query, builder) ->
                    builder.greaterThanOrEqualTo(
                            root.<LocalDate>get("registrationDate"), filter.registrationDateFrom()));
        }
        if (filter.registrationDateTo() != null) {
            specifications.add((root, query, builder) ->
                    builder.lessThanOrEqualTo(
                            root.<LocalDate>get("registrationDate"), filter.registrationDateTo()));
        }

        Specification<EmployeeEntity> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse((root, query, builder) -> builder.conjunction());

        return employeeRepository.findAll(specification, pageable)
                .map(EmployeeEntityMapper::toProfileDto);
    }

    private void addTextFilter(
            List<Specification<EmployeeEntity>> specifications,
            String attribute,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        specifications.add((root, query, builder) ->
                builder.like(builder.lower(root.get(attribute)), pattern));
    }

    /** Возвращает статусы только для сотрудников текущей страницы каталога. */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public java.util.Map<Long, Boolean> getActiveStatuses(Iterable<Long> ids) {
        return employeeRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                        EmployeeEntity::getId, EmployeeEntity::isActive));
    }

    private EmployeeEntity getRequiredEntity(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("Сотрудник с id=" + id + " не найден");
        }
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Сотрудник с id=" + id + " не найден"
                        )
                );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {

        return employeeRepository.existsByEmail(email);
    }

    /**
     * Возвращает DTO текущего сотрудника для границы представления.
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getCurrentEmployee() {
        return EmployeeEntityMapper.toDto(getCurrentEmployeeEntity());
    }

    /**
     * Возвращает доменную модель текущего сотрудника для прикладной логики.
     */
    @Transactional(readOnly = true)
    public com.lb_calc_web.domain.model.Employee getCurrentEmployeeDomain() {
        return EmployeeEntityMapper.toDomain(getCurrentEmployeeEntity());
    }

    private EmployeeEntity getCurrentEmployeeEntity() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new NoSuchElementException(
                    "Пользователь не аутентифицирован"
            );
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof EmployeePrincipal employeePrincipal) {
            return employeeRepository.findById(employeePrincipal.getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Пользователь с id=" + employeePrincipal.getId() + " не найден"
                    ));
        }

        return employeeRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException(
                        "Пользователь " + authentication.getName() + " не найден"
                ));
    }


}