package com.lb_calc_web;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import com.lb_calc_web.entity.ProjectEntity;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.mapper.entity.ProjectEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.repository.ProjectRepository;
import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.equipment.BarReader;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяет сквозное сохранение и восстановление Project → ALS → Module.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenceIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModuleEntityRepository moduleRepository;

    @Autowired
    private ALSRepository alsRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void projectAggregate_shouldRoundTripThroughMySqlWithoutLosingAssociations() {
        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setFirstName("Тест");
        employeeEntity.setLastName("Persistence");
        employeeEntity.setEmail("persistence@example.test");
        employeeEntity.setEncryptedPassword("encoded-password");
        employeeEntity.setRegistrationDate(LocalDate.of(2026, 1, 10));
        employeeEntity.setRole(Role.ROLE_ADMIN);
        employeeEntity = employeeRepository.saveAndFlush(employeeEntity);

        ALS domainALS = TestDataFactory.validALS(null);
        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();

        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                domainALS,
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate)
                        )
                )
        );
        alsEntity = alsRepository.saveAndFlush(alsEntity);

        Employee createdBy = EmployeeEntityMapper.toDomain(employeeEntity);
        Project domainProject = new Project(
                "Persistence round-trip",
                "Test company",
                LocalDate.of(2026, 1, 10),
                createdBy
        );
        domainProject.addALS(domainALS);
        domainProject.addALS(domainALS);

        Long employeeId = employeeEntity.getId();
        Long alsId = alsEntity.getId();

        ProjectEntity projectEntity = ProjectEntityMapper.toEntity(
                domainProject,
                employee -> employeeRepository.findById(employee.getId())
                        .orElseThrow(),
                ignored -> alsRepository.findById(alsId).orElseThrow()
        );
        ProjectEntity savedProject =
                projectRepository.saveAndFlush(projectEntity);

        employeeEntity.setActive(false);
        employeeRepository.saveAndFlush(employeeEntity);

        Long projectId = savedProject.getId();
        entityManager.clear();

        ProjectEntity restoredEntity =
                projectRepository.findById(projectId).orElseThrow();
        Project restoredProject =
                ProjectEntityMapper.toDomain(restoredEntity);

        assertEquals(2, restoredProject.getQuantity(domainALS));
        assertEquals(domainProject.getDescription(), restoredProject.getDescription());

        assertFalse(restoredEntity.getCreatedBy().isActive());
        assertEquals(employeeId, restoredEntity.getCreatedBy().getId());
        assertEquals(employeeId, restoredEntity.getUpdatedBy().getId());

        assertEquals(1, restoredEntity.getAlsEntries().size());
        assertEquals(2, restoredEntity.getAlsEntries().get(0).getQuantity());

        ALSEntity restoredALSEntity =
                restoredEntity.getAlsEntries().get(0).getAls();
        assertEquals(3, restoredALSEntity.getModules().size());
        for (int index = 0; index < restoredALSEntity.getModules().size(); index++) {
            assertEquals(
                    index,
                    restoredALSEntity.getModules().get(index).getModuleOrder()
            );
        }

        LCEntity restoredLC = restoredALSEntity.getModules().stream()
                .map(association -> association.getModule())
                .filter(LCEntity.class::isInstance)
                .map(LCEntity.class::cast)
                .findFirst()
                .orElseThrow();

        assertEquals(2, restoredLC.getEquipment().size());
        assertEquals(alsId, restoredALSEntity.getId());
    }

    @Test
    void lbcAggregate_shouldRoundTripEquipmentQuantitiesAndControlOptions() {
        var lbcALS = TestDataFactory.validLBCALSDTO(2L);
        lbcALS.getLBC().setBarReader(BarReader.READER_1D.getName());
        lbcALS.getLBC().setBarReaderQuantity(2);
        lbcALS.getLBC().setAccessMethods(List.of(AccessMethod.RFID.name()));
        lbcALS.getLBC().setPrintOptions(List.of(PrintOption.BARCODE.name()));

        ALS domainALS = ALSDtoMapper.toDomain(lbcALS);
        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();

        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                domainALS,
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate)
                        )
                )
        );
        Long alsId = alsRepository.saveAndFlush(alsEntity).getId();

        entityManager.clear();

        ALSEntity restoredEntity = alsRepository.findById(alsId).orElseThrow();
        ALS restoredALS = ALSEntityMapper.toDomain(restoredEntity);
        LBCEntity restoredLBC = (LBCEntity) restoredEntity
                .getModules()
                .get(0)
                .getModule();

        assertEquals(domainALS, restoredALS);
        assertEquals(3, restoredLBC.getEquipment().size());
        assertEquals(
                2,
                restoredLBC.getEquipment().stream()
                        .filter(item -> BarReader.READER_1D_ID.equals(
                                item.getCatalogPositionId()))
                        .findFirst()
                        .orElseThrow()
                        .getQuantity()
        );
        assertEquals(Set.of(AccessMethod.RFID), restoredLBC.getAccessMethods());
        assertEquals(Set.of(PrintOption.BARCODE), restoredLBC.getPrintOptions());
    }
}
