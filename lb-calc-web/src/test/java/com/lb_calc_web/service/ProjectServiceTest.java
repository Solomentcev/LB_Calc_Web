package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.mapper.dto.EmployeeDtoMapper;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectEntity;
import org.springframework.security.access.AccessDeniedException;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private ALSRepository alsRepository;
    private EmployeeRepository employeeRepository;
    private ALSService alsService;
    private EmployeeService employeeService;
    private MockEnvironment environment;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        alsRepository = mock(ALSRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        alsService = mock(ALSService.class);
        when(alsService.restoreDomain(any(ALSDTO.class))).thenAnswer(invocation -> {
            ALSDTO dto = invocation.getArgument(0);
            return TestDataFactory.restoreALS(dto);
        });
        employeeService = mock(EmployeeService.class);
environment = new MockEnvironment()
                .withProperty("project.company.default", "TEST_COMPANY")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");

        projectService =
                new ProjectService(
                        projectRepository,
                        alsRepository,
                        employeeRepository,
                        alsService,
                        employeeService,
                        environment,
                        TestDataFactory.createSizeValidator()
                );
    }

    @Test
    void initProject_shouldUseCurrentEmployee() {
        Employee employee = new Employee(1L, "Иван", "Иванов", "ivan@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployeeDomain())
                .thenReturn(employee);

        com.lb_calc_web.domain.model.Project result =
                projectService.initProject("TEST_COMPANY");

        assertEquals("TEST_COMPANY", result.getCompany());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedBy());
        assertTrue(result.getName().startsWith("TEST_COMPANY_"));
    }

    @Test
    void createProject_shouldAddInitialALS() {
        Employee employee = new Employee(1L, "Иван", "Иванов", "ivan@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployeeDomain())
                .thenReturn(employee);

        ALSDTO alsDto = TestDataFactory.validALSDTO(10L);
        com.lb_calc_web.domain.model.ALS als = TestDataFactory.restoreALS(alsDto);

        when(alsService.createALS()).thenReturn(als);

        com.lb_calc_web.domain.model.Project result = projectService.createProject();

        assertEquals(1, result.getQuantity(als));
    }

    @Test
    void saveProject_domain_shouldValidateBeforePersistingALS() {
        Employee employee = new Employee(1L, "Иван", "Иванов", "ivan@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployeeDomain()).thenReturn(employee);

        ProjectDTO dto = TestDataFactory.validProject(1L);
        dto.setId(null);
        dto.setCreatedBy(EmployeeDtoMapper.toDto(employee));
        dto.setUpdatedBy(EmployeeDtoMapper.toDto(employee));
        dto.setCreatedAt(java.time.LocalDate.of(2026, 9, 18));
        dto.setUpdatedAt(java.time.LocalDate.of(2026, 9, 18));
        ALSDTO invalidALS = TestDataFactory.validALSDTO(10L);
        invalidALS.setHeight(10);
        dto.setAlsList(java.util.List.of(invalidALS));

        com.lb_calc_web.domain.model.Project domain =
                ProjectDtoMapper.toDomain(dto, alsService::restoreDomain);

        assertThrows(
                ValidationSizeException.class,
                () -> projectService.saveProject(domain)
        );

        verify(alsService, never()).saveALS(any(com.lb_calc_web.domain.model.ALS.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void deleteById_existing_shouldDelete() {
        ProjectEntity entity = new ProjectEntity();
        Employee admin = new Employee(1L, "Админ", "Админов", "admin@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_ADMIN);

        when(employeeService.getCurrentEmployeeDomain())
                .thenReturn(admin);
        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(entity));

        projectService.deleteById(10L);

        verify(projectRepository).delete(entity);
    }

    @Test
    void saveLBAtProject_missingProject_shouldRejectBeforeRestoringReplacement() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> projectService.saveLBAtProject(
                        1L,
                        10L,
                        100L,
                        LBDtoMapper.toDomain(TestDataFactory.validLBDTO(100L))
                )
        );

        verify(alsService, never()).restoreLB(any(LBDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void replaceALSandSaveProject_missingProject_shouldRejectBeforeRestoringALS() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        ALSDTO replacementDto = TestDataFactory.validALSDTO(10L);
        com.lb_calc_web.domain.model.ALS replacement = TestDataFactory.restoreALS(replacementDto);

        assertThrows(
                NoSuchElementException.class,
                () -> projectService.replaceALSandSaveProject(1L, 10L, replacement)
        );

        verify(alsService, never()).restoreDomain(any(ALSDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void previewProject_shouldValidateWithoutPersisting() {
        Employee employee = new Employee(
                1L,
                "Иван",
                "Иванов",
                "preview@example.test",
                java.time.LocalDate.of(2026, 9, 18),
                Role.ROLE_MANAGER
        );
        com.lb_calc_web.domain.model.Project requested =
                new com.lb_calc_web.domain.model.Project(
                        "Проект",
                        "TEST_COMPANY",
                        java.time.LocalDate.of(2026, 9, 18),
                        employee
                );
        requested.addALS(TestDataFactory.validALS(10L));

        com.lb_calc_web.domain.model.Project result =
                projectService.previewProject(requested);

        assertSame(requested, result);
        verify(projectRepository, never()).save(any(ProjectEntity.class));
        verify(alsService, never()).saveALS(any(com.lb_calc_web.domain.model.ALS.class));
    }

    @Test
    void findAll_shouldReturnDomainProjects() {
        when(projectRepository.findAllWithUsers()).thenReturn(java.util.List.of());
        assertTrue(projectService.findAll().isEmpty());
    }

    @Test
    void findById_missingProject_shouldThrow() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> projectService.findById(1L));
    }

    @Test
    void findALSInProject_missingProject_shouldThrow() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> projectService.findALSInProject(1L, 10L));
    }

    @Test
    void saveProject_domain_managerNotOwner_shouldRejectBeforeSavingALS() {
        ProjectEntity existing = mock(ProjectEntity.class);
        EmployeeEntity owner = mock(EmployeeEntity.class);
        when(existing.getCreatedBy()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        Employee manager = new Employee(11L, "Иван", "Иванов", "ivan@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployeeDomain()).thenReturn(manager);

        ProjectDTO dto = TestDataFactory.validProject(1L);
        dto.setId(1L);
        Employee ownerDomain = new Employee(10L, "Владелец", "Проекта",
                "owner@example.com", java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);
        dto.setCreatedBy(EmployeeDtoMapper.toDto(ownerDomain));
        dto.setUpdatedBy(EmployeeDtoMapper.toDto(ownerDomain));
        dto.setCreatedAt(java.time.LocalDate.of(2026, 9, 18));
        dto.setUpdatedAt(java.time.LocalDate.of(2026, 9, 18));
        com.lb_calc_web.domain.model.Project project =
                ProjectDtoMapper.toDomain(dto, alsService::restoreDomain);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.saveProject(project)
        );

        verify(alsService, never()).saveALS(any(com.lb_calc_web.domain.model.ALS.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void deleteById_managerNotOwner_shouldReject() {
        ProjectEntity existing = mock(ProjectEntity.class);
        EmployeeEntity owner = mock(EmployeeEntity.class);
        when(existing.getCreatedBy()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        Employee manager = new Employee(11L, "Иван", "Иванов", "ivan@example.com",
                java.time.LocalDate.of(2026, 9, 18), Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployeeDomain()).thenReturn(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteById(1L)
        );

        verify(projectRepository, never()).delete(any(ProjectEntity.class));
    }

}
