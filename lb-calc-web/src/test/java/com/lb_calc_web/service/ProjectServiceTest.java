package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectEntity;
import org.springframework.security.access.AccessDeniedException;
import com.lb_calc_web.handler.ValidationSizeException;
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
    private EquipmentCatalogService equipmentCatalogService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        alsRepository = mock(ALSRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        alsService = mock(ALSService.class);
        when(alsService.restoreDomain(any(ALSDTO.class))).thenAnswer(invocation -> {
            ALSDTO dto = invocation.getArgument(0);
            return ALSDtoMapper.toDomain(
                    dto,
                    value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                            value.getShelfThick(), value.getServiceZoneWidth()),
                    value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                            value.getShelfThick(), value.getServiceZoneWidth())
            );
        });
        employeeService = mock(EmployeeService.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);
        when(equipmentCatalogService.findDomainCatalogResolver())
                .thenReturn(EquipmentCatalog::findByCatalogPositionId);

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
                        TestDataFactory.createSizeValidator(),
                        equipmentCatalogService
                );
    }

    @Test
    void initProject_shouldUseCurrentEmployee() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setRegistrationDate(
                java.time.LocalDate.of(2026, 9, 18)
        );
        employee.setRole(Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployee())
                .thenReturn(employee);

        ProjectDTO result =
                projectService.initProject("TEST_COMPANY");

        assertEquals("TEST_COMPANY", result.getCompany());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getCreatedBy());
        assertTrue(
                result.getName()
                        .startsWith("TEST_COMPANY_")
        );
    }

    @Test
    void createProject_shouldAddInitialALS() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setRegistrationDate(
                java.time.LocalDate.of(2026, 9, 18)
        );
        employee.setRole(Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployee())
                .thenReturn(employee);

        ALSDTO als = TestDataFactory.validALSDTO(10L);

        when(alsService.createALS())
                .thenReturn(als);

        ProjectDTO result =
                projectService.createProject();

        assertEquals(1, result.getAlsList().size());
        assertSame(als, result.getAlsList().get(0));
    }

    @Test
    void saveProject_invalidGeometry_shouldRejectBeforeSavingALS() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setRegistrationDate(
                java.time.LocalDate.of(2026, 9, 18)
        );
        employee.setRole(Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployee())
                .thenReturn(employee);

        ProjectDTO project = TestDataFactory.validProject(1L);
        project.setId(null);

        ALSDTO invalidALS = TestDataFactory.validALSDTO(10L);
        invalidALS.setHeight(10);
        project.setAlsList(java.util.List.of(invalidALS));

        EmployeeDTO originalCreatedBy = new EmployeeDTO();
        originalCreatedBy.setId(7L);
        EmployeeDTO originalUpdatedBy = new EmployeeDTO();
        originalUpdatedBy.setId(8L);
        project.setCreatedBy(originalCreatedBy);
        project.setCreatedAt(java.time.LocalDate.of(2025, 1, 2));
        project.setUpdatedBy(originalUpdatedBy);
        project.setUpdatedAt(java.time.LocalDate.of(2025, 2, 3));
        var originalAlsList = project.getAlsList();

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> projectService.saveProject(project)
        );

        var alsHeightError = exception.getValidationResult()
                .getErrors()
                .stream()
                .filter(error -> "ALS".equals(error.getObjectType())
                        && "height".equals(error.getField()))
                .findFirst()
                .orElseThrow();

        assertEquals(10L, alsHeightError.getObjectId());
        assertSame(originalCreatedBy, project.getCreatedBy());
        assertEquals(java.time.LocalDate.of(2025, 1, 2), project.getCreatedAt());
        assertSame(originalUpdatedBy, project.getUpdatedBy());
        assertEquals(java.time.LocalDate.of(2025, 2, 3), project.getUpdatedAt());
        assertSame(originalAlsList, project.getAlsList());
        verify(alsService, never()).saveALS(any(ALSDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void saveProject_invalidDomainMapping_shouldRejectBeforeSavingALS() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setRegistrationDate(
                java.time.LocalDate.of(2026, 9, 18)
        );
        employee.setRole(Role.ROLE_MANAGER);

        when(employeeService.getCurrentEmployee())
                .thenReturn(employee);

        ProjectDTO project = TestDataFactory.validProject(1L);
        project.setId(null);

        ALSDTO invalidALS = TestDataFactory.validALSDTO(11L);
        invalidALS.setColorBody(null);
        project.setAlsList(java.util.List.of(invalidALS));

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> projectService.saveProject(project)
        );

        var error = exception.getValidationResult().getErrors().get(0);
        assertEquals("Project", error.getObjectType());
        assertEquals("projectData", error.getField());
        verify(alsService, never()).saveALS(any(ALSDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void deleteById_existing_shouldDelete() {
        ProjectEntity entity = new ProjectEntity();
        EmployeeDTO admin = new EmployeeDTO();
        admin.setId(1L);
        admin.setRole(Role.ROLE_ADMIN);

        when(employeeService.getCurrentEmployee())
                .thenReturn(admin);
        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(entity));

        projectService.deleteById(10L);

        verify(projectRepository).delete(entity);
    }

    @Test
    void saveLBAtProject_shouldAcceptNewPersistenceIdAfterConfigurationChange() {
        ProjectDTO project = new ProjectDTO();
        project.setId(1L);

        ALSDTO als = TestDataFactory.validALSDTO(10L);
        project.getAlsList().add(als);
        Long oldLbId = als.getLbList().get(0).getId();
        LBDTO requestedLB = TestDataFactory.validLBDTO(oldLbId);

        ALSDTO updatedALS = new ALSDTO();
        ALSDTO savedALS = new ALSDTO();
        savedALS.setLbList(java.util.List.of(
                TestDataFactory.validLBDTO(901L),
                TestDataFactory.validLBDTO(902L)
        ));

        ProjectService spy = spy(projectService);
        doReturn(project).when(spy).findById(1L);
        when(alsService.replaceLB(als, requestedLB)).thenReturn(updatedALS);
        doReturn(savedALS).when(spy).replaceALSandSaveProject(
                project,
                updatedALS,
                10L
        );

        ALSDTO result = spy.saveLBAtProject(
                1L,
                10L,
                oldLbId,
                requestedLB
        );

        assertSame(savedALS, result);
        assertNotEquals(oldLbId, result.getLbList().get(0).getId());
    }

    @Test
    void findLBCInProject_shouldReturnLBC() {
        ALSDTO als = TestDataFactory.validLBCALSDTO(10L);
        LBCDTO lbc = als.getLBC();

        ProjectDTO project = new ProjectDTO();
        project.setId(1L);
        project.getAlsList().add(als);

        ProjectService spy = spy(projectService);
        doReturn(project).when(spy).findById(1L);

        LBCDTO result =
                spy.findLBCInProject(
                        1L,
                        10L,
                        lbc.getId()
                );

        assertSame(lbc, result);
    }

    @Test
    void findLBCInProject_missing_shouldThrow() {
        ProjectDTO project =
                TestDataFactory.validProject(1L);

        ProjectService spy = spy(projectService);
        doReturn(project).when(spy).findById(1L);

        assertThrows(
                NoSuchElementException.class,
                () -> spy.findLBCInProject(
                        1L,
                        999L,
                        999L
                )
        );
    }

    @Test
    void saveProject_managerNotOwner_shouldRejectBeforeSavingALS() {
        ProjectEntity existing = mock(ProjectEntity.class);
        EmployeeEntity owner = mock(EmployeeEntity.class);
        when(existing.getCreatedBy()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeDTO manager = new EmployeeDTO();
        manager.setId(11L);
        manager.setRole(Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployee()).thenReturn(manager);

        ProjectDTO project = new ProjectDTO();
        project.setId(1L);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.saveProject(project)
        );

        verify(alsService, never()).saveALS(any(ALSDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void deleteById_managerNotOwner_shouldReject() {
        ProjectEntity existing = mock(ProjectEntity.class);
        EmployeeEntity owner = mock(EmployeeEntity.class);
        when(existing.getCreatedBy()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeDTO manager = new EmployeeDTO();
        manager.setId(11L);
        manager.setRole(Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployee()).thenReturn(manager);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteById(1L)
        );

        verify(projectRepository, never()).delete(any(ProjectEntity.class));
    }

}
