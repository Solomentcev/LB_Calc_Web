package com.lb_calc_web.service;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.ALSDTO;
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
import com.lb_calc_web.helper.ExcellHelper;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.mapper.entity.ProjectEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.repository.ProjectRepository;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

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

    public ProjectService(
            ProjectRepository projectRepository,
            ALSRepository alsRepository,
            EmployeeRepository employeeRepository,
            ALSService alsService,
            EmployeeService employeeService,
            Environment environment
    ) {
        this.projectRepository = projectRepository;
        this.alsRepository = alsRepository;
        this.employeeRepository = employeeRepository;
        this.alsService = alsService;
        this.employeeService = employeeService;
        this.environment = environment;
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> findAll() {
        logger.info("Получение списка проектов...");

        return projectRepository
                .findAllWithUsers()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDTO findById(Long id) {
        logger.info(
                "Поиск проекта(id={})...",
                id
        );

        ProjectEntity entity =
                projectRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Проект с id "
                                                + id
                                                + " не найден"
                                )
                        );

        return toDto(entity);
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
     * 3. Сохраняем ALS и получаем реальные ID.
     * 4. Валидируем проект.
     * 5. DTO -> domain.
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

        ProjectEntity existing =
                loadExistingProject(
                        dto.getId()
                );

        prepareMetadata(
                dto,
                existing
        );

        List<ALSDTO> savedALS =
                saveALS(
                        dto.getAlsList()
                );

        dto.setAlsList(
                savedALS
        );

        validateProject(
                dto
        );

        Project domain =
                ProjectDtoMapper.toDomain(
                        dto
                );

        Map<ALS, ALSEntity> alsEntities =
                resolveALSEntities(
                        savedALS
                );

        ProjectEntity entity;

        if (existing == null) {
            entity =
                    ProjectEntityMapper.toEntity(
                            domain,
                            this::resolveEmployee,
                            alsEntities
                    );
        } else {
            entity = existing;

            ProjectEntityMapper.updateEntity(
                    domain,
                    entity,
                    this::resolveEmployee,
                    alsEntities
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

        return toDto(saved);
    }

    @Transactional
    public void deleteById(
            Long id
    ) {
        logger.info(
                "Удаление проекта(id={})...",
                id
        );

        ProjectEntity entity =
                projectRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Проект с id "
                                                + id
                                                + " не найден"
                                )
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

        return ExcellHelper.projectToExcel(
                projectDTO
        );
    }

    /**
     * Добавляет новый ALS в проект.
     */
    @Transactional
    public ProjectDTO addNewALSandSaveProject(
            Long projectId
    ) {
        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                alsService.createALS();

        project.getAlsList().add(als);

        return saveProject(project);
    }

    /**
     * Удаляет ALS из проекта.
     */
    @Transactional
    public ProjectDTO deleteALSandSaveProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

        findALSInProject(
                projectId,
                alsId
        );

        boolean removed =
                project.getAlsList()
                        .removeIf(
                                als ->
                                        als != null
                                                && Objects.equals(
                                                als.getId(),
                                                alsId
                                        )
                        );

        if (!removed) {
            throw new NoSuchElementException(
                    "ALS с id "
                            + alsId
                            + " не найден в проекте "
                            + projectId
            );
        }

        return saveProject(project);
    }

    /**
     * Находит ALS внутри конкретного проекта.
     */
    @Transactional(readOnly = true)
    public ALSDTO findALSInProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

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
                                        + projectId
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
        ALSDTO als =
                findALSInProject(
                        projectId,
                        alsId
                );

        LCDTO lc =
                als.getLC();

        if (lc == null
                || !Objects.equals(
                lc.getId(),
                lcId
        )) {
            throw new NoSuchElementException(
                    "LC с id "
                            + lcId
                            + " не найден в ALS "
                            + alsId
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
        ALSDTO als =
                findALSInProject(
                        projectId,
                        alsId
                );

        LBCDTO lbc =
                als.getLBC();

        if (lbc == null
                || !Objects.equals(
                lbc.getId(),
                lbcId
        )) {
            throw new NoSuchElementException(
                    "LBC с id "
                            + lbcId
                            + " не найден в ALS "
                            + alsId
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
        ALSDTO als =
                findALSInProject(
                        projectId,
                        alsId
                );

        return als.getLbList()
                .stream()
                .filter(
                        lb ->
                                lb != null
                                        && Objects.equals(
                                        lb.getId(),
                                        lbId
                                )
                )
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "LB с id "
                                        + lbId
                                        + " не найден в ALS "
                                        + alsId
                        )
                );
    }

    /**
     * Заменяет ALS внутри проекта и сохраняет проект.
     */
    @Transactional
    public ALSDTO replaceALSandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId
    ) {
        Objects.requireNonNull(
                project,
                "Проект не должен быть null"
        );

        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        logger.info(
                "Замена ALS(id={}) в проекте(id={})...",
                alsId,
                project.getId()
        );

        List<ALSDTO> alsList =
                project.getAlsList();

        boolean replaced = false;

        for (int i = 0;
             i < alsList.size();
             i++) {

            ALSDTO current =
                    alsList.get(i);

            if (current != null
                    && Objects.equals(
                    current.getId(),
                    alsId
            )) {
                als.setId(alsId);

                alsList.set(
                        i,
                        als
                );

                replaced = true;
                break;
            }
        }

        if (!replaced) {
            throw new NoSuchElementException(
                    "ALS с id "
                            + alsId
                            + " не найден в проекте "
                            + project.getId()
            );
        }

        saveProject(project);

        return als;
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
                alsService.replaceLCandSaveALS(
                        als,
                        lc
                );

        replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );

        return updatedALS;
    }

    /**
     * Сохраняет LC конкретного ALS конкретного проекта.
     */
    @Transactional
    public LCDTO saveLCAtProject(
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
                findALSInProject(
                        projectId,
                        alsId
                );

        findLCInProject(
                projectId,
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

        return Objects.requireNonNull(
                updatedALS.getLC(),
                "После сохранения LC отсутствует в ALS"
        );
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
                alsService.replaceLBCandSaveALS(
                        als,
                        lbc
                );

        replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );

        return updatedALS;
    }

    /**
     * Сохраняет LBC конкретного ALS конкретного проекта.
     */
    @Transactional
    public LBCDTO saveLBCAtProject(
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
                findALSInProject(
                        projectId,
                        alsId
                );

        findLBCInProject(
                projectId,
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

        return Objects.requireNonNull(
                updatedALS.getLBC(),
                "После сохранения LBC отсутствует в ALS"
        );
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
                findALSInProject(
                        projectId,
                        alsId
                );

        ALSDTO updatedALS =
                alsService.replaceWithNewLBCandSaveALS(
                        alsId
                );

        replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );

        return updatedALS;
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

        findALSInProject(
                projectId,
                alsId
        );

        ALSDTO als =
                alsService.addNewLBandSaveALS(
                        alsId
                );

        replaceALSandSaveProject(
                project,
                als,
                alsId
        );

        return als;
    }

    /**
     * Сохраняет LB конкретного ALS конкретного проекта.
     */
    @Transactional
    public LBDTO saveLBAtProject(
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
                findALSInProject(
                        projectId,
                        alsId
                );

        findLBInProject(
                projectId,
                alsId,
                lbId
        );

        lb.setId(lbId);

        List<LBDTO> lbList =
                new ArrayList<>(
                        als.getLbList()
                );

        boolean replaced = false;

        for (int i = 0;
             i < lbList.size();
             i++) {

            LBDTO current =
                    lbList.get(i);

            if (current != null
                    && Objects.equals(
                    current.getId(),
                    lbId
            )) {
                lbList.set(
                        i,
                        lb
                );

                replaced = true;
                break;
            }
        }

        if (!replaced) {
            throw new NoSuchElementException(
                    "LB с id "
                            + lbId
                            + " не найден в ALS "
                            + alsId
            );
        }

        als.setLbList(lbList);

        ALSDTO updatedALS =
                alsService.saveALS(als);

        replaceALSandSaveProject(
                project,
                updatedALS,
                alsId
        );

        return updatedALS.getLbList()
                .stream()
                .filter(
                        value ->
                                value != null
                                        && Objects.equals(
                                        value.getId(),
                                        lbId
                                )
                )
                .findFirst()
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "После сохранения LB с id "
                                        + lbId
                                        + " не найден"
                        )
                );
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

        findLBInProject(
                projectId,
                alsId,
                lbId
        );

        ALSDTO als =
                alsService.deleteLBandSaveALS(
                        alsId,
                        lbId
                );

        replaceALSandSaveProject(
                project,
                als,
                alsId
        );

        return als;
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
            ProjectEntity existing
    ) {
        var currentEmployee =
                employeeService.getCurrentEmployee();

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
        dto.setCreatedBy(
                EmployeeDtoMapper.toDto(
                        EmployeeEntityMapper.toDomain(
                                existing.getCreatedBy()
                        )
                )
        );

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
    private Map<ALS, ALSEntity> resolveALSEntities(
            List<ALSDTO> savedALS
    ) {
        Map<ALS, ALSEntity> result =
                new LinkedHashMap<>();

        if (savedALS == null
                || savedALS.isEmpty()) {
            return result;
        }

        Set<Long> ids =
                new LinkedHashSet<>();

        for (ALSDTO dto : savedALS) {

            if (dto == null) {
                throw new IllegalArgumentException(
                        "ALS проекта не должен быть null"
                );
            }

            Long id =
                    dto.getId();

            if (id == null || id <= 0) {
                throw new IllegalStateException(
                        "После сохранения ALS не получил корректный id"
                );
            }

            ids.add(id);
        }

        List<ALSEntity> entities =
                alsRepository.findAllById(ids);

        Map<Long, ALSEntity> entitiesById =
                new HashMap<>();

        for (ALSEntity entity : entities) {
            entitiesById.put(
                    entity.getId(),
                    entity
            );
        }

        for (Long id : ids) {

            if (!entitiesById.containsKey(id)) {
                throw new NoSuchElementException(
                        "ALS с id "
                                + id
                                + " не найдена в БД"
                );
            }
        }

        for (ALSDTO dto : savedALS) {

            ALS domain =
                    ALSDtoMapper.toDomain(dto);

            ALSEntity entity =
                    entitiesById.get(
                            dto.getId()
                    );

            result.put(
                    domain,
                    entity
            );
        }

        return result;
    }

    private void validateProject(
            ProjectDTO dto
    ) {
        List<ValidationResult> results =
                SizeValidator.validateProject(
                        dto
                );

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
                        dto.getId()
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

        return employeeRepository
                .findByEmail(
                        domain.getEmail()
                )
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "Сотрудник с email "
                                        + domain.getEmail()
                                        + " не найден"
                        )
                );
    }

    /**
     * Entity -> DTO.
     */
    private ProjectDTO toDto(
            ProjectEntity entity
    ) {
        Project domain =
                ProjectEntityMapper.toDomain(
                        entity
                );

        Map<ALS, Long> alsIds =
                new LinkedHashMap<>();

        for (var entry :
                entity.getAlsEntries()) {

            ALS als =
                    ALSEntityMapper.toDomain(
                            entry.getAls()
                    );

            alsIds.put(
                    als,
                    entry.getAls().getId()
            );
        }

        ProjectDTO dto =
                ProjectDtoMapper.toDto(
                        domain,
                        alsIds::get
                );

        dto.setId(
                entity.getId()
        );

        return dto;
    }

    private String getRequired(
            String key
    ) {
        return environment.getRequiredProperty(
                key
        );
    }
}