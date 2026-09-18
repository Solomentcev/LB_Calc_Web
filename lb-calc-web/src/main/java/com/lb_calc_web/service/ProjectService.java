package com.lb_calc_web.service;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.ALSDTO;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
                .sorted(
                        Comparator.comparing(
                                ProjectEntity::getId
                        )
                )
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDTO findById(
            Long id
    ) {
        logger.info(
                "Поиск проекта(id={})...",
                id
        );

        ProjectEntity entity =
                projectRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Проект с id"
                                                + id
                                                + " не найден"
                                )
                        );

        return toDto(entity);
    }

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

        project.getAlsList().add(
                als
        );

        rebuildQuantityALS(
                project
        );

        updateDescription(
                project
        );

        return project;
    }

    public ProjectDTO initProject(
            String company
    ) {
        Employee employee =
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
                        employee
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

    @Transactional
    public ProjectDTO saveProject(
            ProjectDTO dto
    ) {
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
                saveALS(dto.getAlsList());

        dto.setAlsList(
                savedALS
        );

        rebuildQuantityALS(
                dto
        );

        updateDescription(
                dto
        );

        validateProject(
                dto
        );

        Project domain =
                ProjectDtoMapper.toDomain(
                        dto
                );

        ProjectEntity entity;

        if (existing == null) {
            entity =
                    ProjectEntityMapper.toEntity(
                            domain,
                            this::resolveEmployee,
                            this::resolveALS
                    );
        } else {
            entity = existing;

            ProjectEntityMapper.updateEntity(
                    domain,
                    entity,
                    this::resolveEmployee,
                    this::resolveALS
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
                                        "Проект с id"
                                                + id
                                                + " не найден"
                                )
                        );

        projectRepository.delete(
                entity
        );
    }

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

    @Transactional
    public ProjectDTO addNewALSandSaveProject(
            Long projectId
    ) {
        ProjectDTO project =
                findById(projectId);

        ALSDTO als =
                alsService.createALS();

        project.getAlsList().add(
                als
        );

        rebuildQuantityALS(project);
        updateDescription(project);

        return saveProject(project);
    }

    @Transactional
    public ProjectDTO deleteALSandSaveProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

        boolean removed =
                project.getAlsList()
                        .removeIf(
                                als ->
                                        als.getId() != null
                                                && als.getId().equals(alsId)
                        );

        if (!removed) {
            throw new NoSuchElementException(
                    "ALS с id"
                            + alsId
                            + " не найден в проекте "
                            + projectId
            );
        }

        rebuildQuantityALS(project);
        updateDescription(project);

        return saveProject(project);
    }

    @Transactional
    public ALSDTO replaceALSandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId
    ) {
        logger.info(
                "Замена ALS(id={}) в проекте(id={})...",
                alsId,
                project.getId()
        );

        boolean replaced =
                false;

        List<ALSDTO> alsList =
                project.getAlsList();

        for (int i = 0;
             i < alsList.size();
             i++) {

            ALSDTO current =
                    alsList.get(i);

            if (current.getId() != null
                    && current.getId().equals(alsId)) {

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
                    "ALS с id"
                            + alsId
                            + " не найден в проекте"
            );
        }

        rebuildQuantityALS(project);
        updateDescription(project);

        saveProject(project);

        return als;
    }

    @Transactional
    public ALSDTO replaceLCandSaveProject(
            ProjectDTO project,
            ALSDTO als,
            Long alsId,
            com.lb_calc_web.dto.LCDTO lc
    ) {
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

    @Transactional
    public ALSDTO addLBAtProject(
            Long projectId,
            Long alsId
    ) {
        ProjectDTO project =
                findById(projectId);

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

    @Transactional
    public ALSDTO deleteLBatProject(
            Long projectId,
            Long alsId,
            Long lbId
    ) {
        ProjectDTO project =
                findById(projectId);

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
                                "Проект с id"
                                        + id
                                        + " не найден"
                        )
                );
    }

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

            if (dto.getCreatedBy() == null) {
                dto.setCreatedBy(
                        currentEmployee
                );
            }

            if (dto.getCreatedAt() == null) {
                dto.setCreatedAt(
                        LocalDate.now()
                );
            }

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

        } else {

            if (dto.getCreatedBy() == null) {
                dto.setCreatedBy(
                        EmployeeDtoMapper.toDto(
                                EmployeeEntityMapper.toDomain(
                                        existing.getCreatedBy()
                                )
                        )
                );
            }

            if (dto.getCreatedAt() == null) {
                dto.setCreatedAt(
                        existing.getCreatedAt()
                );
            }

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
    }

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
                continue;
            }

            result.add(
                    alsService.saveALS(
                            als
                    )
            );
        }

        return result;
    }

    private void rebuildQuantityALS(
            ProjectDTO dto
    ) {
        Map<ALSDTO, Integer> quantity =
                new LinkedHashMap<>();

        if (dto.getAlsList() != null) {
            for (ALSDTO als :
                    dto.getAlsList()) {

                if (als == null) {
                    continue;
                }

                quantity.merge(
                        als,
                        1,
                        Integer::sum
                );
            }
        }

        dto.setQuantityALS(
                quantity
        );
    }

    private void updateDescription(
            ProjectDTO dto
    ) {
        StringBuilder description =
                new StringBuilder();

        for (Map.Entry<ALSDTO, Integer> entry :
                dto.getQuantityALS().entrySet()) {

            description
                    .append(
                            entry.getKey().getName()
                    )
                    .append(" - ")
                    .append(
                            entry.getValue()
                    )
                    .append(" шт.\n");
        }

        dto.setDescription(
                description.toString()
        );
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

        if (!errors.isEmpty()) {

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
    }

    private EmployeeEntity resolveEmployee(
            Employee domain
    ) {
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

    private ALSEntity resolveALS(
            ALS domain
    ) {
        List<ALSEntity> entities =
                alsRepository.findAll();

        for (ALSEntity entity :
                entities) {

            try {
                ALS existing =
                        ALSEntityMapper.toDomain(
                                entity
                        );

                if (domain.equals(existing)) {
                    return entity;
                }

            } catch (RuntimeException e) {

                logger.warn(
                        "Не удалось сравнить ALS id={}",
                        entity.getId(),
                        e
                );
            }
        }

        throw new IllegalStateException(
                "ALS для проекта не найдена в БД"
        );
    }

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