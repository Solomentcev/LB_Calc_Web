package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.entity.ProjectEntity;
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
        employeeService = mock(EmployeeService.class);

        environment = new MockEnvironment()
                .withProperty("project.company.default", "TEST_COMPANY");

        projectService =
                new ProjectService(
                        projectRepository,
                        alsRepository,
                        employeeRepository,
                        alsService,
                        employeeService,
                        environment
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
    void deleteById_existing_shouldDelete() {
        ProjectEntity entity = new ProjectEntity();
        when(projectRepository.findById(10L))
                .thenReturn(Optional.of(entity));

        projectService.deleteById(10L);

        verify(projectRepository).delete(entity);
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
}
