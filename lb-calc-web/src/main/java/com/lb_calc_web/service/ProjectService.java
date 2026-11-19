package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationError;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.service.export.ProjectExcelExportService;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
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

import java.io.ByteArrayInputStream;
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

    private final ProjectRepository projectRepository;
    private final ALSRepository alsRepository;
    private final EmployeeRepository employeeRepository;
    private final ALSService alsService;
    private final EmployeeService employeeService;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentCatalogService equipmentCatalogService;
    private final ProjectExcelExportService projectExcelExportService;

    public ProjectService(
            ProjectRepository projectRepository,
            ALSRepository alsRepository,
            EmployeeRepository employeeRepository,
            ALSService alsService,
            EmployeeService employeeService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentCatalogService equipmentCatalogService,
            ProjectExcelExportService projectExcelExportService
    ) {
        this.projectRepository = projectRepository;
        this.alsRepository = alsRepository;
        this.employeeRepository = employeeRepository;
        this.alsService = alsService;
        this.employeeService = employeeService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
        this.equipmentCatalogService = equipmentCatalogService;
        this.projectExcelExportService = projectExcelExportService;
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        logger.info("Получение списка проектов...");
        Function<String, Equipment> equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        return projectRepository
                .findAllWithUsers()
                .stream()
                .map(entity -> ProjectEntityMapper.toDto(entity, equipmentResolver))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDTO findById(Long id) {
        logger.info(
                "Поиск проекта(id={})...",
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

        return ProjectEntityMapper.toDto(entity, equipmentCatalogService.findDomainCatalogResolver());
    }

    /**
     * Создаёт DTO для формы создания проекта.
     *
     * Сам проект пока не сохраняется.
     * Сохраняется только при submit формы.
     */
    @Transactional
    public ProjectDTO createProject() {
        logger.info("Создание проекта...");

        String company =
                getRequired(
                        "project.company.default"
                );

        ProjectDTO project =
                initProject(company);

        ALSDTO als =
                alsService.createALS();

        project.getAlsList().add(als);

        return project;
    }

    /**
     * Инициализация нового проекта.
     */
    public ProjectDTO initProject(
            String company
    ) {
        Employee currentEmployee =
                EmployeeDtoMapper.toDomain(
                        employeeService.getCurrentEmployee()
                );

        LocalDate now =
                LocalDate.now();

        Project project =
                new Project(
                        company,
                        company,
                        now,
                        currentEmployee
                );

        ProjectDTO dto =
                ProjectDtoMapper.toDto(
                        project
                );

        dto.setId(0L);

        dto.setName(
                company + "_" + now
        );

        return dto;
    }

    /**
     * Сохраняет проект.
     *
     * Общая схема:
     *
     * 1. Загружаем существующий проект, если это update.
     * 2. Устанавливаем серверные metadata.
     * 3. Преобразуем DTO в доменный проект.
     * 4. Валидируем доменную модель до сохранения новых ALS.
     * 5. Сохраняем ALS и получаем реальные ID.
     * 6. Разрешаем ALS -> ALSEntity.
     * 7. Domain -> ProjectEntity.
     * 8. Сохраняем ProjectEntity.
     */
    @Transactional
    public ProjectDTO saveProject(
            ProjectDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "Проект не должен быть null"
        );

        logger.info(
                "Сохранение проекта(id={}, name={})...",
                dto.getId(),
                dto.getName()
        );

        ProjectDTO working = ProjectDtoMapper.copy(dto);

        ProjectEntity existing =
                loadExistingProject(
                        working.getId()
                );

        EmployeeDTO currentEmployee =
                employeeService.getCurrentEmployee();

        assertCanModifyProject(
                existing,
                currentEmployee
        );

        prepareMetadata(
                working,
                existing,
                currentEmployee
        );

        /*
         * Сначала преобразуем DTO в доменный агрегат и проверяем его.
         * До успешной валидации не сохраняем новые конфигурации ALS.
         */
        Project domain;
        try {
            domain = ProjectDtoMapper.toDomain(working, alsDto -> alsService.restoreDomain(alsDto));
        } catch (IllegalStateException exception) {
            ValidationResult validationResult =
                    new ValidationResult("Project", working.getId());
            validationResult.addError(
                    "alsStructure",
                    exception.getMessage(),
                    null,
                    null,
                    null
            );
            throw new ValidationSizeException(validationResult);
        } catch (IllegalArgumentException | NullPointerException exception) {
            ValidationResult validationResult =
                    new ValidationResult("Project", working.getId());
            validationResult.addError(
                    "projectData",
                    exception.getMessage(),
                    null,
                    null,
                    null
            );
            throw new ValidationSizeException(validationResult);
        }

        validateProject(domain, working);

        List<ALSDTO> savedALS =
                saveALS(
                        working.getAlsList()
                );

        working.setAlsList(savedALS);

        /*
         * ALSService возвращает канонические DTO с конструктивными
         * параметрами, разрешёнными из конфигурации приложения.
         * Повторно собираем Project, чтобы его ALS совпадали с теми,
         * для которых ниже разрешаются persistence-сущности.
         */
        domain = ProjectDtoMapper.toDomain(working, alsDto -> alsService.restoreDomain(alsDto));

        Function<ALS, ALSEntity> alsResolver =
                resolveALSEntities(
                        savedALS
                );

        ProjectEntity entity;

        if (existing == null) {
            entity =
                    ProjectEntityMapper.toEntity(
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

        ProjectEntity saved =
                projectRepository.save(
                        entity
                );

        logger.info(
                "Проект сохранён: id={}",
                saved.getId()
        );

        return ProjectEntityMapper.toDto(saved, equipmentCatalogService.findDomainCatalogResolver());
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
                employeeService.getCurrentEmployee()
        );

        projectRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportToExcel(
            ProjectDTO projectDTO
    ) {
        logger.info(
                "Экспорт проекта в Excel..."
        );

        return projectExcelExportService.exportToExcel(projectDTO);
    }

    /**
     * Добавляет одну единицу новой конфигурации ALS в проект.
     */
    @Transactional
    public ProjectDTO addNewALSandSaveProject(Long projectId) {
        ProjectDTO source = findById(projectId);
        Project domain = ProjectDtoMapper.toDomain(source, alsDto -> alsService.restoreDomain(alsDto));

        domain.addALS(
                alsService.restoreDomain(alsService.createALS())
        );

        return saveProject(ProjectDtoMapper.toDto(domain, source, alsDto -> alsService.restoreDomain(alsDto)));
    }

    /**
     * Удаляет одну единицу выбранной конфигурации ALS из проекта.
     */
    @Transactional
    public ProjectDTO deleteALSandSaveProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO source = findById(projectId);
        ALSDTO selected = findALSInProject(source, alsId);

        Project domain = ProjectDtoMapper.toDomain(source, alsDto -> alsService.restoreDomain(alsDto));
        ALS selectedDomain = alsService.restoreDomain(selected);

        if (domain.getQuantity(selectedDomain) == 0) {
            throw new NoSuchElementException(
                    "ALS с id " + alsId
                            + " не найден в проекте " + projectId
            );
        }

        domain.removeALS(selectedDomain);

        return saveProject(ProjectDtoMapper.toDto(domain, source, alsDto -> alsService.restoreDomain(alsDto)));
    }

    /**
     * Находит ALS внутри конкретного проекта.
     */
    @Transactional(readOnly = true)
    public ALSDTO findALSInProject(
            Long projectId,
            Long alsId
    ) {
        return findALSInProject(
                findById(projectId),
                alsId
        );
    }

    /**
     * Находит конфигурацию ALS в уже загруженном проекте.
     */
    private ALSDTO findALSInProject(
            ProjectDTO project,
            Long alsId
    ) {
        return project.getAlsList()
                .stream()
                .filter(
                        als ->
                                als != null
                                        && Objects.equals(
                                        als.getId(),
                                        alsId
                                )
                )
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "ALS с id "
                                        + alsId
                                        + " не найден в проекте "
                                        + project.getId()
                        )
                );
    }

    /**
     * Находит LC внутри конкретного ALS конкретного проекта.
     */
    @Transactional(readOnly = true)
    public LCDTO findLCInProject(
            Long projectId,
            Long alsId,
            Long lcId
    ) {
        return findLCInProject(findById(projectId), alsId, lcId);
    }

    private LCDTO findLCInProject(
            ProjectDTO project,
            Long alsId,
            Long lcId
    ) {
        LCDTO lc = findALSInProject(project, alsId).getLC();

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
    public LBCDTO findLBCInProject(
            Long projectId,
            Long alsId,
            Long lbcId
    ) {
        return findLBCInProject(findById(projectId), alsId, lbcId);
    }

    private LBCDTO findLBCInProject(
            ProjectDTO project,
            Long alsId,
            Long lbcId
    ) {
        LBCDTO lbc = findALSInProject(project, alsId).getLBC();

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
    public LBDTO findLBInProject(
            Long projectId,
            Long alsId,
            Long lbId
    ) {
        return findLBInProject(findById(projectId), alsId, lbId);
    }

    private LBDTO findLBInProject(
            ProjectDTO project,
            Long alsId,
            Long lbId
    ) {
        return findALSInProject(project, alsId)
                .getLbList()
                .stream()
                .filter(lb -> lb != null && Objects.equals(lb.getId(), lbId))
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "LB с id " + lbId + " не найден в ALS " + alsId
                        )
                );
    }

    /**
     * Заменяет одну единицу ALS в доменном составе проекта.
     *
     * <p>Состав изменяется через доменную модель Project.
     * Сохранение проекта выполняется одной транзакцией.</p>
     */
    @Transactional
    public ALSDTO replaceALSandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId
    ) {
        Objects.requireNonNull(project, "Проект не должен быть null");
        Objects.requireNonNull(als, "ALS не должен быть null");

        ALSDTO selected = project.getAlsList().stream()
                .filter(value -> value != null
                        && Objects.equals(value.getId(), alsId))
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "ALS с id " + alsId
                                        + " не найден в проекте "
                                        + project.getId()
                        )
                );

        Project domain = ProjectDtoMapper.toDomain(project, alsDto -> alsService.restoreDomain(alsDto));
        ALS oldConfiguration = alsService.restoreDomain(selected);
        ALS newConfiguration = alsService.restoreDomain(als);

        domain.replaceALS(oldConfiguration, newConfiguration);

        ProjectDTO savedProject = saveProject(ProjectDtoMapper.toDto(domain, project, alsDto -> alsService.restoreDomain(alsDto)));

        return savedProject.getAlsList().stream()
                .filter(value -> value != null
                        && alsService.restoreDomain(value).equals(newConfiguration))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(
                                "После сохранения изменённая ALS отсутствует в проекте"
                        )
                );
    }

    /**
     * Заменяет LC в ALS и сохраняет проект.
     */
    @Transactional
    public ALSDTO replaceLCandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId,
            LCDTO lc
    ) {
        Objects.requireNonNull(
                lc,
                "LC не должен быть null"
        );

        ALSDTO updatedALS =
                alsService.replaceLC(als, lc);

        return replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );
    }

    /**
     * Сохраняет LC конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALSDTO saveLCAtProject(
            Long projectId,
            Long alsId,
            Long lcId,
            LCDTO lc
    ) {
        Objects.requireNonNull(
                lc,
                "LC не должен быть null"
        );

        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                findALSInProject(project, alsId);

        findLCInProject(
                project,
                alsId,
                lcId
        );

        lc.setId(lcId);

        ALSDTO updatedALS =
                replaceLCandSaveProject(
                        project,
                        als,
                        alsId,
                        lc
                );

        Objects.requireNonNull(
                updatedALS.getLC(),
                "После сохранения LC отсутствует в ALS"
        );
        return updatedALS;
    }

    /**
     * Заменяет LBC в ALS и сохраняет проект.
     */
    @Transactional
    public ALSDTO replaceLBCandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId,
            LBCDTO lbc
    ) {
        Objects.requireNonNull(
                lbc,
                "LBC не должен быть null"
        );

        ALSDTO updatedALS =
                alsService.replaceLBC(als, lbc);

        return replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );
    }

    /**
     * Сохраняет LBC конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALSDTO saveLBCAtProject(
            Long projectId,
            Long alsId,
            Long lbcId,
            LBCDTO lbc
    ) {
        Objects.requireNonNull(
                lbc,
                "LBC не должен быть null"
        );

        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                findALSInProject(project, alsId);

        findLBCInProject(
                project,
                alsId,
                lbcId
        );

        lbc.setId(lbcId);

        ALSDTO updatedALS =
                replaceLBCandSaveProject(
                        project,
                        als,
                        alsId,
                        lbc
                );

        Objects.requireNonNull(
                updatedALS.getLBC(),
                "После сохранения LBC отсутствует в ALS"
        );
        return updatedALS;
    }

    /**
     * Заменяет текущий control-модуль проекта на новый LBC.
     */
    @Transactional
    public ALSDTO replaceWithNewLBCAtProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                findALSInProject(project, alsId);

        ALSDTO updatedALS =
                alsService.replaceWithNewLBC(als);

        return replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );
    }

    /**
     * Добавляет LB в конкретный ALS конкретного проекта.
     */
    @Transactional
    public ALSDTO addLBAtProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

        ALSDTO currentALS =
                findALSInProject(project, alsId);

        ALSDTO als =
                alsService.addNewLB(currentALS);

        return replaceALSandSaveProject(
                project,
                als,
                alsId
        );
    }

    /**
     * Сохраняет LB конкретного ALS конкретного проекта.
     */
    @Transactional
    public ALSDTO saveLBAtProject(
            Long projectId,
            Long alsId,
            Long lbId,
            LBDTO lb
    ) {
        Objects.requireNonNull(
                lb,
                "LB не должен быть null"
        );

        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                findALSInProject(project, alsId);

        findLBInProject(
                project,
                alsId,
                lbId
        );

        lb.setId(lbId);

        ALSDTO updatedALS =
                alsService.replaceLB(als, lb);

        ALSDTO savedALS =
                replaceALSandSaveProject(
                        project,
                        updatedALS,
                        alsId
                );

        /*
         * После изменения конфигурации LB получает новый persistence ID.
         * Проверять прежний ID нельзя: он идентифицировал исходную запись,
         * а не новую конфигурацию.
         */
        if (savedALS.getLbList() == null
                || savedALS.getLbList().size() != als.getLbList().size()) {
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
    public ALSDTO deleteLBatProject(
            Long projectId,
            Long alsId,
            Long lbId
    ) {
        ProjectDTO project =
                findById(projectId);

        findLBInProject(project, alsId, lbId);

        ALSDTO als =
                alsService.deleteLB(
                        findALSInProject(project, alsId),
                        lbId
                );

        return replaceALSandSaveProject(
                project,
                als,
                alsId
        );
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
            EmployeeDTO currentEmployee
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
     * Metadata проекта контролируются сервером.
     *
     * CREATE:
     * - createdBy = текущий сотрудник
     * - createdAt = текущая дата
     *
     * UPDATE:
     * - createdBy/createdAt берутся из БД
     * - updatedBy = текущий сотрудник
     * - updatedAt = текущая дата
     */
    private void prepareMetadata(
            ProjectDTO dto,
            ProjectEntity existing,
            EmployeeDTO currentEmployee
    ) {
        dto.setUpdatedBy(
                currentEmployee
        );

        dto.setUpdatedAt(
                LocalDate.now()
        );

        if (existing == null) {

            dto.setCreatedBy(
                    currentEmployee
            );

            dto.setCreatedAt(
                    LocalDate.now()
            );

            if (dto.getCompany() == null
                    || dto.getCompany().isBlank()) {

                dto.setCompany(
                        getRequired(
                                "project.company.default"
                        )
                );
            }

            if (dto.getName() == null
                    || dto.getName().isBlank()) {

                dto.setName(
                        dto.getCompany()
                                + "_"
                                + dto.getCreatedAt()
                );
            }

            return;
        }

        /*
         * При update эти поля не должны приходить
         * от клиента как источник истины.
         */
        dto.setCreatedBy(employeeService.loadUserById(existing.getCreatedBy().getId()));

        dto.setCreatedAt(
                existing.getCreatedAt()
        );

        if (dto.getCompany() == null
                || dto.getCompany().isBlank()) {

            dto.setCompany(
                    existing.getCompany()
            );
        }

        if (dto.getName() == null
                || dto.getName().isBlank()) {

            dto.setName(
                    existing.getName()
            );
        }
    }

    /**
     * Сохраняет все ALS, входящие в проект.
     *
     * После сохранения DTO обязательно получают
     * актуальные persistence ID.
     */
    private List<ALSDTO> saveALS(
            List<ALSDTO> source
    ) {
        if (source == null) {
            return List.of();
        }

        List<ALSDTO> result =
                new ArrayList<>(
                        source.size()
                );

        for (ALSDTO als : source) {

            if (als == null) {
                throw new IllegalArgumentException(
                        "ALS проекта не должен быть null"
                );
            }

            result.add(
                    alsService.saveALS(
                            als
                    )
            );
        }

        return result;
    }

    /**
     * Разрешает ALS из DTO в ALSEntity.
     *
     * Для связанной сущности нам нужен именно persistence ID,
     * поэтому здесь нет сравнения ALS по конфигурации.
     */
    /**
     * Создаёт функцию разрешения доменной ALS в сохранённую ALSEntity.
     *
     * <p>Сопоставление выполняется по бизнес-содержимому через список
     * пар, а не через Map с изменяемыми доменными ключами.</p>
     */
    private Function<ALS, ALSEntity> resolveALSEntities(
            List<ALSDTO> savedALS
    ) {
        if (savedALS == null || savedALS.isEmpty()) {
            return als -> {
                throw new NoSuchElementException(
                        "В проекте отсутствует сохранённая конфигурация ALS"
                );
            };
        }

        Set<Long> ids = new LinkedHashSet<>();

        for (ALSDTO dto : savedALS) {
            if (dto == null) {
                throw new IllegalArgumentException(
                        "ALS проекта не должен быть null"
                );
            }

            Long id = dto.getId();

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

        return ProjectEntityMapper.createAlsResolver(
                savedALS,
                entitiesById,
                alsDto -> alsService.restoreDomain(alsDto)
        );
    }

    private void validateProject(Project domain, ProjectDTO source) {
        List<ValidationResult> results =
                sizeValidator.validateProject(domain);
        ProjectDtoMapper.assignValidationObjectIds(results, domain, source, alsDto -> alsService.restoreDomain(alsDto));

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


    private String getRequired(
            String key
    ) {
        return environment.getRequiredProperty(
                key
        );
    }
}