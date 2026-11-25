package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.service.validation.ValidationError;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectEntity;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.mapper.entity.ProjectEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.repository.ProjectRepository;
import com.lb_calc_web.service.validation.size.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
public class ProjectService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProjectService.class);

    private static final String DEFAULT_COMPANY = "Заказчик";

    private final ProjectRepository projectRepository;
    private final ALSRepository alsRepository;
    private final EmployeeRepository employeeRepository;
    private final ALSService alsService;
    private final EmployeeService employeeService;
    private final Environment environment;
    private final SizeValidator sizeValidator;

    public ProjectService(
            ProjectRepository projectRepository,
            ALSRepository alsRepository,
            EmployeeRepository employeeRepository,
            ALSService alsService,
            EmployeeService employeeService,
            Environment environment,
            SizeValidator sizeValidator
    ) {
        this.projectRepository = projectRepository;
        this.alsRepository = alsRepository;
        this.employeeRepository = employeeRepository;
        this.alsService = alsService;
        this.employeeService = employeeService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Project> findPage(
            String query, org.springframework.data.domain.Pageable pageable
    ) {
        String term = query == null ? "" : query.trim();
        var entities = term.isEmpty()
                ? projectRepository.findAllWithUsers(pageable)
                : projectRepository.findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        term, term, term, pageable);
        return entities.map(entity -> ProjectEntityMapper.toDomain(
                entity, (als, alsEntity) -> { }));
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        logger.info("Получение списка проектов...");
return projectRepository.findAllWithUsers()
                .stream()
                .map(entity -> ProjectEntityMapper.toDomain(
                        entity,
                        (als, alsEntity) -> { }
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        logger.info("Поиск проекта(id={})...", id);
        ProjectEntity entity = loadExistingProject(id);
        if (entity == null) {
            throw new NoSuchElementException("Проект с id " + id + " не найден");
        }

        return ProjectEntityMapper.toDomain(
                entity,
                (als, alsEntity) -> { }
        );
    }

    /**
     * Создаёт доменный проект с начальной конфигурацией ALS.
     * Сохранение выполняется только после отправки формы.
     */
    @Transactional
    public Project createProject() {
        logger.info("Создание проекта...");

        String company = getDefaultCompany();
        Project project = initProject(company);
        project.addALS(alsService.createALS());
        return project;
    }

    /**
     * Инициализирует новый доменный проект.
     */
    public Project initProject(String company) {
        Employee currentEmployee = employeeService.getCurrentEmployeeDomain();
        LocalDate today = LocalDate.now();

        Project project = new Project(
                company,
                company,
                today,
                currentEmployee
        );
        project.setName(company + "_" + today);
        return project;
    }

    /**
     * Сохраняет доменный проект. Серверные metadata формируются
     * на основании сохранённой записи и текущего сотрудника.
     */
    @Transactional
    public Project saveProject(Project requested) {
        Objects.requireNonNull(requested, "Проект не должен быть null");

        ProjectEntity existing = loadExistingProject(requested.getId());
        Employee currentEmployee = employeeService.getCurrentEmployeeDomain();
        assertCanModifyProject(existing, currentEmployee);
        LocalDate today = LocalDate.now();

        String company = existing == null
                ? requested.getCompany()
                : existing.getCompany();
        if (company == null || company.isBlank()) {
            company = getDefaultCompany();
        }

        String name = requested.getName();
        if (name == null || name.isBlank()) {
            name = existing == null
                    ? company + "_" + today
                    : existing.getName();
        }

        LocalDate createdAt = existing == null
                ? today
                : existing.getCreatedAt();
        Employee createdBy = existing == null
                ? currentEmployee
                : com.lb_calc_web.mapper.entity.EmployeeEntityMapper.toDomain(
                        existing.getCreatedBy()
                );

        Project domain = Project.restore(
                name,
                company,
                createdAt,
                createdBy,
                today,
                currentEmployee,
                requested.getQuantityALS()
        );
        domain.setId(existing == null ? null : existing.getId());

        validateProject(domain);
        ProjectEntity saved = persistProjectEntity(domain, existing);
        return ProjectEntityMapper.toDomain(
                saved,
                (als, alsEntity) -> { }
        );
    }

    @Transactional
    public void deleteById(
            Long id
    ) {
        logger.info(
                "Удаление проекта(id={})...",
                id
        );
        if (id == null || id <= 0) {
            throw new NoSuchElementException("Проект с id " + id + " не найден");
        }

        ProjectEntity entity =
                projectRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Проект с id "
                                                + id
                                                + " не найден"
                                )
                        );

        assertCanModifyProject(
                entity,
                employeeService.getCurrentEmployeeDomain()
        );

        projectRepository.delete(entity);
    }

    /**
     * Добавляет одну единицу новой конфигурации ALS в проект.
     */
    @Transactional
    public Project addNewALSandSaveProject(Long projectId) {
        Project domain = findDomainProject(projectId);
        domain.addALS(alsService.createALS());
        return saveDomainProject(domain);
    }

    /**
     * Удаляет одну единицу выбранной конфигурации ALS из проекта.
     */
    @Transactional
    public Project deleteALSandSaveProject(
            Long projectId,
            Long alsId
    ) {
        Project domain = findDomainProject(projectId);
        ALS selectedDomain = findDomainALS(domain, alsId);

        if (domain.getQuantity(selectedDomain) == 0) {
            throw new NoSuchElementException(
                    "ALS с id " + alsId
                            + " не найден в проекте " + projectId
            );
        }

        domain.removeALS(selectedDomain);

        return saveDomainProject(domain);
    }

    /**
     * Находит ALS внутри конкретного проекта.
     */
    @Transactional(readOnly = true)
    public ALS findALSInProject(Long projectId, Long alsId) {
        return findDomainALS(findDomainProject(projectId), alsId);
    }

    /**
     * Находит LC внутри конкретного ALS конкретного проекта.
     */
    @Transactional(readOnly = true)
    public LC findLCInProject(Long projectId, Long alsId, Long lcId) {
        ALS als = findALSInProject(projectId, alsId);
        LC lc = als.getLC();
        if (lc == null || !Objects.equals(lc.getId(), lcId)) {
            throw new NoSuchElementException(
                    "LC с id " + lcId + " не найден в ALS " + alsId
            );
        }
        return lc;
    }

    /**
     * Находит LBC внутри конкретного ALS конкретного проекта.
     */
    @Transactional(readOnly = true)
    public LBC findLBCInProject(Long projectId, Long alsId, Long lbcId) {
        ALS als = findALSInProject(projectId, alsId);
        LBC lbc = als.getLBC();
        if (lbc == null || !Objects.equals(lbc.getId(), lbcId)) {
            throw new NoSuchElementException(
                    "LBC с id " + lbcId + " не найден в ALS " + alsId
            );
        }
        return lbc;
    }

    /**
     * Находит LB внутри конкретного ALS конкретного проекта.
     */
    @Transactional(readOnly = true)
    public LB findLBInProject(Long projectId, Long alsId, Long lbId) {
        ALS als = findALSInProject(projectId, alsId);
        return als.getStorageModules().stream()
                .filter(LB.class::isInstance)
                .map(LB.class::cast)
                .filter(lb -> Objects.equals(lb.getId(), lbId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "LB с id " + lbId + " не найден в ALS " + alsId
                ));
    }

    /**
     * Заменяет одну единицу ALS в доменном составе проекта.
     *
     * <p>Состав изменяется через доменную модель Project.
     * Сохранение проекта выполняется одной транзакцией.</p>
     */
    @Transactional
    public ALS replaceALSandSaveProject(
            Long projectId,
            Long alsId,
            ALS replacement
    ) {
        Objects.requireNonNull(replacement, "ALS не должен быть null");

        Project domain = findDomainProject(projectId);
        ALS oldConfiguration = findDomainALS(domain, alsId);

        /*
         * ID определяется URL, а не телом запроса.
         * Это исключает подмену идентификатора ALS клиентом.
         */
        replacement.setId(alsId);
        domain.replaceALS(oldConfiguration, replacement);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, replacement);
    }

    /**
     * Сохраняет LC конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALS saveLCAtProject(
            Long projectId,
            Long alsId,
            Long lcId,
            LC replacement
    ) {
        Objects.requireNonNull(
                replacement,
                "LC не должен быть null"
        );

        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        LC current = original.getLC();
        if (current == null || !Objects.equals(current.getId(), lcId)) {
            throw new NoSuchElementException(
                    "LC с id " + lcId + " не найден в ALS " + alsId
            );
        }

        replacement.setId(lcId);
        ALS updated = alsService.replaceLC(original.copy(), replacement);
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, updated);
    }

    /**
     * Сохраняет LBC конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALS saveLBCAtProject(
            Long projectId,
            Long alsId,
            Long lbcId,
            LBC replacement
    ) {
        Objects.requireNonNull(
                replacement,
                "LBC не должен быть null"
        );

        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        LBC current = original.getLBC();
        if (current == null || !Objects.equals(current.getId(), lbcId)) {
            throw new NoSuchElementException(
                    "LBC с id " + lbcId + " не найден в ALS " + alsId
            );
        }

        replacement.setId(lbcId);
        ALS updated = alsService.replaceLBC(original.copy(), replacement);
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, updated);
    }

    /**
     * Заменяет текущий control-модуль проекта на новый LBC.
     */
    @Transactional
    public ALS replaceWithNewLBCAtProject(
            Long projectId,
            Long alsId
    ) {
        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        ALS updated = alsService.replaceWithNewLBC(original.copy());
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, updated);
    }

    /**
     * Добавляет LB в конкретный ALS конкретного проекта.
     */
    @Transactional
    public ALS addLBAtProject(
            Long projectId,
            Long alsId
    ) {
        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        ALS updated = alsService.addNewLB(original.copy());
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, updated);
    }

    /**
     * Сохраняет LB конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALS saveLBAtProject(
            Long projectId,
            Long alsId,
            Long lbId,
            LB replacement
    ) {
        Objects.requireNonNull(
                replacement,
                "LB не должен быть null"
        );

        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        if (original.getStorageModules().stream()
                .filter(LB.class::isInstance)
                .map(LB.class::cast)
                .noneMatch(module -> Objects.equals(module.getId(), lbId))) {
            throw new NoSuchElementException(
                    "LB с id " + lbId + " не найден в ALS " + alsId
            );
        }

        int originalLbCount = (int) original.getStorageModules().stream()
                .filter(LB.class::isInstance)
                .count();

        replacement.setId(lbId);
        ALS updated = alsService.replaceLB(original.copy(), lbId, replacement);
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        ALS savedALS = findDomainALS(savedProject, updated);

        /*
         * После изменения конфигурации LB получает новый persistence ID.
         * Проверять прежний ID нельзя: он идентифицировал исходную запись,
         * а не новую конфигурацию.
         */
        long savedLbCount = savedALS.getStorageModules().stream()
                .filter(LB.class::isInstance)
                .count();
        if (savedLbCount != originalLbCount) {
            throw new IllegalStateException(
                    "После сохранения изменился состав LB в ALS"
            );
        }

        return savedALS;
    }

    /**
     * Удаляет LB из конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALS deleteLBatProject(
            Long projectId,
            Long alsId,
            Long lbId
    ) {
        Project domain = findDomainProject(projectId);
        ALS original = findDomainALS(domain, alsId);
        ALS updated = alsService.deleteLB(original.copy(), lbId);
        domain.replaceALS(original, updated);

        Project savedProject = saveDomainProject(domain);
        return findDomainALS(savedProject, updated);
    }


    /**
     * Загружает проект непосредственно в доменную модель.
     *
     * <p>Для восстановления конфигураций используется полный каталог,
     * включая деактивированные позиции, необходимые историческим проектам.</p>
     */
    private Project findDomainProject(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new NoSuchElementException("Проект с id " + projectId + " не найден");
        }

        ProjectEntity entity = loadExistingProject(projectId);
        return ProjectEntityMapper.toDomain(
                entity,
                (als, alsEntity) -> { }
        );
    }

    /**
     * Находит конкретную конфигурацию ALS по persistence ID в агрегате проекта.
     */
    private ALS findDomainALS(Project project, Long alsId) {
        return project.getQuantityALS().keySet().stream()
                .filter(als -> Objects.equals(als.getId(), alsId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "ALS с id " + alsId + " не найден в проекте " + project.getId()
                ));
    }

    /**
     * Находит сохранённую конфигурацию ALS по её доменному содержимому.
     *
     * <p>Project возвращает независимые копии ключей Map, поэтому ID,
     * назначенный при сохранении копии, нельзя читать из исходного объекта.</p>
     */
    private ALS findDomainALS(Project project, ALS configuration) {
        return project.getQuantityALS().keySet().stream()
                .filter(als -> als.equals(configuration))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "После сохранения изменённая ALS отсутствует в проекте"
                ));
    }

    /**
     * Сохраняет уже изменённый доменный проект.
     */
    private Project saveDomainProject(Project domain) {
        ProjectEntity existing = loadExistingProject(domain.getId());
        Employee currentEmployee = employeeService.getCurrentEmployeeDomain();
        assertCanModifyProject(existing, currentEmployee);

        domain.setUpdatedBy(currentEmployee);
        validateProject(domain);

        ProjectEntity saved = persistProjectEntity(domain, existing);
        return ProjectEntityMapper.toDomain(
                saved,
                (als, alsEntity) -> { }
        );
    }

    /**
     * Сохраняет доменный проект и все уникальные конфигурации ALS.
     */
    private ProjectEntity persistProjectEntity(Project domain, ProjectEntity existing) {
        /*
         * Project агрегирует одинаковые ALS, поэтому каждая уникальная
         * конфигурация сохраняется ровно один раз независимо от количества.
         */
        List<ALS> savedALS = new ArrayList<>(domain.getQuantityALS().keySet());
        savedALS.forEach(alsService::saveALS);

        Function<ALS, ALSEntity> alsResolver = resolveALSEntities(savedALS);
        ProjectEntity entity;

        if (existing == null) {
            entity = ProjectEntityMapper.toEntity(
                    domain,
                    this::resolveEmployee,
                    alsResolver
            );
        } else {
            entity = existing;
            ProjectEntityMapper.updateEntity(
                    domain,
                    entity,
                    this::resolveEmployee,
                    alsResolver
            );
        }

        ProjectEntity saved = projectRepository.save(entity);
        logger.info("Проект сохранён: id={}", saved.getId());
        return saved;
    }


    private ProjectEntity loadExistingProject(
            Long id
    ) {
        if (id == null || id <= 0) {
            return null;
        }

        return projectRepository.findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Проект с id "
                                        + id
                                        + " не найден"
                        )
                );
    }

    /**
     * Проверяет право текущего сотрудника изменять проект.
     *
     * <p>Администратор может изменять любой проект.
     * Менеджер может изменять только проекты, которые он создал.</p>
     *
     * @param project существующий проект или {@code null} при создании
     * @param currentEmployee текущий сотрудник
     * @throws AccessDeniedException если изменение запрещено
     */
    private void assertCanModifyProject(
            ProjectEntity project,
            Employee currentEmployee
    ) {
        if (project == null) {
            return;
        }

        if (currentEmployee == null
                || currentEmployee.getRole() == null) {
            throw new AccessDeniedException(
                    "Недостаточно прав для изменения проекта"
            );
        }

        if (currentEmployee.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        boolean isOwner =
                currentEmployee.getRole() == Role.ROLE_MANAGER
                        && Objects.equals(
                                currentEmployee.getId(),
                                project.getCreatedBy().getId()
                        );

        if (!isOwner) {
            throw new AccessDeniedException(
                    "Менеджер может изменять только собственные проекты"
            );
        }
    }

    /**
     * Создаёт функцию разрешения доменной ALS в сохранённую ALSEntity.
     *
     * <p>Каждая уникальная конфигурация уже сохранена через Domain API.
     * Для связывания с проектом загружаются только сущности по полученным ID.</p>
     */
    private Function<ALS, ALSEntity> resolveALSEntities(
            List<ALS> savedALS
    ) {
        if (savedALS == null || savedALS.isEmpty()) {
            return als -> {
                throw new NoSuchElementException(
                        "В проекте отсутствует сохранённая конфигурация ALS"
                );
            };
        }

        Set<Long> ids = new LinkedHashSet<>();

        for (ALS als : savedALS) {
            if (als == null) {
                throw new IllegalArgumentException(
                        "ALS проекта не должен быть null"
                );
            }

            Long id = als.getId();
            if (id == null || id <= 0) {
                throw new IllegalStateException(
                        "После сохранения ALS не получил корректный id"
                );
            }
            ids.add(id);
        }

        Map<Long, ALSEntity> entitiesById = new HashMap<>();
        for (ALSEntity entity : alsRepository.findAllById(ids)) {
            entitiesById.put(entity.getId(), entity);
        }

        for (Long id : ids) {
            if (!entitiesById.containsKey(id)) {
                throw new NoSuchElementException(
                        "ALS с id " + id + " не найдена в БД"
                );
            }
        }

        List<Map.Entry<ALS, ALSEntity>> resolved = new ArrayList<>();
        for (ALS als : savedALS) {
            ALSEntity entity = entitiesById.get(als.getId());
            if (resolved.stream().noneMatch(entry -> entry.getKey().equals(als))) {
                resolved.add(Map.entry(als, entity));
            }
        }

        return requested -> resolved.stream()
                .filter(entry -> entry.getKey().equals(requested))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Для конфигурации ALS не найдена сохранённая сущность"));
    }

    private void validateProject(Project domain) {
        List<ValidationResult> results =
                sizeValidator.validateProject(domain);

        List<ValidationError> errors =
                results.stream()
                        .filter(
                                result ->
                                        !result.isValid()
                        )
                        .flatMap(
                                result ->
                                        result.getErrors()
                                                .stream()
                        )
                        .toList();

        if (errors.isEmpty()) {
            return;
        }

        ValidationResult combined =
                new ValidationResult(
                        "Project",
                        null
                );

        errors.forEach(
                combined::addError
        );

        throw new ValidationSizeException(
                combined
        );
    }

    /**
     * DTO/domain Employee -> JPA EmployeeEntity.
     */
    private EmployeeEntity resolveEmployee(
            Employee domain
    ) {
        if (domain == null) {
            throw new IllegalArgumentException(
                    "Employee не должен быть null"
            );
        }

        if (domain.getId() == null) {
            throw new IllegalArgumentException(
                    "У сотрудника отсутствует ID"
            );
        }

        return employeeRepository
                .findById(domain.getId())
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Сотрудник с ID "
                                        + domain.getId()
                                        + " не найден"
                        )
                );
    }


    /**
     * Возвращает значение компании по умолчанию.
     *
     * <p>Если внешняя настройка содержит строку с типичными признаками
     * неверно декодированной кириллицы, используется безопасное значение
     * по умолчанию. Это предотвращает сохранение новых проектов с кракозябрами.</p>
     *
     * @return название компании по умолчанию
     */
    private String getDefaultCompany() {
        String configured = environment.getProperty(
                "project.company.default",
                DEFAULT_COMPANY
        );

        if (configured.contains("Ð")
                || configured.contains("Ñ")
                || configured.contains("Ã")
                || configured.contains("Â")) {
            logger.warn(
                    "Настройка project.company.default содержит некорректно декодированный текст; "
                            + "используется значение по умолчанию"
            );
            return DEFAULT_COMPANY;
        }

        return configured;
    }
}