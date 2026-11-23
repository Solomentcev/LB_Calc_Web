package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.service.export.ProjectExcelExportService;
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
    private ProjectExcelExportService projectExcelExportService;

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
        projectExcelExportService = mock(ProjectExcelExportService.class);
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
                        equipmentCatalogService,
                        projectExcelExportService
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
    void saveProject_domain_shouldValidateBeforePersistingALS() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(1L);
        employee.setFirstName("Иван");
        employee.setLastName("Иванов");
        employee.setEmail("ivan@example.com");
        employee.setRegistrationDate(
                java.time.LocalDate.of(2026, 9, 18)
        );
        employee.setRole(Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployee()).thenReturn(employee);

        ProjectDTO dto = TestDataFactory.validProject(1L);
        dto.setId(null);
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
    void saveLBAtProject_missingProject_shouldRejectBeforeRestoringReplacement() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> projectService.saveLBAtProject(
                        1L,
                        10L,
                        100L,
                        TestDataFactory.validLBDTO(100L)
                )
        );

        verify(alsService, never()).restoreLB(any(LBDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void replaceALSandSaveProject_missingProject_shouldRejectBeforeRestoringALS() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        ALSDTO replacement = TestDataFactory.validALSDTO(10L);

        assertThrows(
                NoSuchElementException.class,
                () -> projectService.replaceALSandSaveProject(1L, 10L, replacement)
        );

        verify(alsService, never()).restoreDomain(any(ALSDTO.class));
        verify(projectRepository, never()).save(any(ProjectEntity.class));
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
    void saveProject_domain_managerNotOwner_shouldRejectBeforeSavingALS() {
        ProjectEntity existing = mock(ProjectEntity.class);
        EmployeeEntity owner = mock(EmployeeEntity.class);
        when(existing.getCreatedBy()).thenReturn(owner);
        when(owner.getId()).thenReturn(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeDTO manager = new EmployeeDTO();
        manager.setId(11L);
        manager.setRole(Role.ROLE_MANAGER);
        when(employeeService.getCurrentEmployee()).thenReturn(manager);

        ProjectDTO dto = TestDataFactory.validProject(1L);
        dto.setId(1L);
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
