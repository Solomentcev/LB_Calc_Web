package com.lb_calc_web;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.ProjectService;
import com.lb_calc_web.service.ALSService;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.ModuleEntity;
import com.lb_calc_web.entity.ProjectEntity;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.mapper.entity.ProjectEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.repository.ProjectRepository;
import com.lb_calc_web.domain.attributes.Role;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.lb_calc_web.security.EmployeePrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяет сквозное сохранение и восстановление Project → ALS → Module.
 */
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ModuleEntityRepository moduleRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentCatalogService equipmentCatalogService;

    @Autowired
    private ALSRepository alsRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ALSService alsService;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveAls_shouldRollbackWhenEquipmentIdCannotBeResolved() {
        long moduleCountBefore = moduleRepository.count();
        long alsCountBefore = alsRepository.count();

        ALS domainALS = restoreAlsDto(TestDataFactory.validLBCALSDTO(2L));
        domainALS.addStorageModule(TestDataFactory.validLB(null));

        Equipment missingEquipment = new Equipment(
                999999L,
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID,
                EquipmentCategory.RFID_READER,
                "Отсутствующая модель",
                "",
                120,
                50,
                30
        );
        domainALS.setControlConfiguration(new ControlConfiguration(
                domainALS.getFunctionalConfiguration(),
                Map.of(missingEquipment, 1)
        ));

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> alsService.saveALS(domainALS)
        );

        assertEquals(moduleCountBefore, moduleRepository.count(),
                "Модули, сохранённые до ошибки, должны откатиться вместе с транзакцией");
        assertEquals(alsCountBefore, alsRepository.count(),
                "ALS не должна сохраняться при ошибке разрешения оборудования");
    }

    @Test
    void equipmentCatalog_shouldRoundTripAndRejectDeletionWhileInUse() {
        Equipment initial = persistEquipment(
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER,
                "Сканер до изменения"
        );

        Equipment updated = new Equipment(
                initial.getId(),
                initial.getFunctionalType(),
                initial.getPurpose(),
                initial.getCategory(),
                "Сканер после изменения",
                "Обновлённое описание",
                121,
                51,
                31
        );
        Equipment saved = equipmentCatalogService.save(updated);

        assertEquals(initial.getId(), saved.getId());
        assertEquals("Сканер после изменения", saved.getName());
        assertEquals("Обновлённое описание", saved.getDescription());
        assertEquals(121, saved.getWidth());

        ALSDTO dto = TestDataFactory.validLBCALSDTO(2L);
        FunctionalConfigurationDTO lbcFunctional = dto.getLBC().getFunctionalConfiguration();
        lbcFunctional.setDisplay(DisplayPurpose.INCH_10);
        lbcFunctional.setAccess(Set.of(AccessPurpose.BAR_CODE));
        lbcFunctional.setPayment(PaymentPurpose.NONE);
        lbcFunctional.setPrinter(PrinterPurpose.ABSENT);
        dto.getLBC().setSelectedEquipment(List.of(TestDataFactory.selectedEquipment(saved.getId(), 1)));
        dto.getLBC().setEquipmentProvided(true);

        ALS domainALS = restoreAlsDto(dto);
        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();
        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                domainALS,
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate, equipment -> equipmentRepository.findById(equipment.getId()).orElseThrow())
                        )
                )
        );
        alsRepository.saveAndFlush(alsEntity);
        entityManager.clear();

        Equipment restored = equipmentCatalogService.findById(saved.getId());
        assertEquals("Сканер после изменения", restored.getName());
        assertEquals(AccessPurpose.BAR_CODE, restored.getPurpose());

        Equipment changedAfterUse = new Equipment(
                saved.getId(),
                saved.getFunctionalType(),
                saved.getPurpose(),
                saved.getCategory(),
                "Переименование после установки",
                saved.getDescription(),
                saved.getWidth(),
                saved.getHeight(),
                saved.getDepth()
        );
        assertThrows(
                IllegalStateException.class,
                () -> equipmentCatalogService.save(changedAfterUse)
        );

        assertThrows(
                IllegalStateException.class,
                () -> equipmentCatalogService.delete(saved.getId())
        );
        assertTrue(equipmentRepository.existsById(saved.getId()));
    }

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
        domainALS.addStorageModule(TestDataFactory.validLB(301L));
        domainALS.addStorageModule(TestDataFactory.validLB(302L));
        domainALS.addStorageModule(TestDataFactory.validLB(303L));
        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();

        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                domainALS,
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate, equipment -> equipmentRepository.findById(equipment.getId()).orElseThrow())
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
                restoreProjectEntity(restoredEntity);

        assertEquals(2, restoredProject.getQuantity(domainALS));
        assertEquals(domainProject.getDescription(), restoredProject.getDescription());

        assertFalse(restoredEntity.getCreatedBy().isActive());
        assertEquals(employeeId, restoredEntity.getCreatedBy().getId());
        assertEquals(employeeId, restoredEntity.getUpdatedBy().getId());

        assertEquals(1, restoredEntity.getAlsEntries().size());
        assertEquals(2, restoredEntity.getAlsEntries().get(0).getQuantity());

        ALSEntity restoredALSEntity =
                restoredEntity.getAlsEntries().get(0).getAls();
        assertEquals(6, restoredALSEntity.getModules().size());
        for (int index = 0; index < restoredALSEntity.getModules().size(); index++) {
            assertEquals(
                    index,
                    restoredALSEntity.getModules().get(index).getModuleOrder()
            );
        }

        ALS restoredALS = restoreAlsEntity(restoredALSEntity);
        assertEquals(domainALS, restoredALS);
        assertEquals("CENTER", restoredALS.getPositionControlModule().name());

        LCEntity restoredLC = restoredALSEntity.getModules().stream()
                .map(association ->
                        (ModuleEntity) Hibernate.unproxy(association.getModule()))
                .filter(LCEntity.class::isInstance)
                .map(LCEntity.class::cast)
                .findFirst()
                .orElseThrow();

        assertEquals(0, restoredLC.getEquipment().size());
        assertEquals(alsId, restoredALSEntity.getId());
    }

    @Test
    void lbcAggregate_shouldRoundTripEquipmentQuantitiesAndControlOptions() {
        var lbcALS = TestDataFactory.validLBCALSDTO(2L);
        Equipment display = persistEquipment(
                EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10");
        Equipment barcode = persistEquipment(
                EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер штрихкода");
        Equipment rfid = persistEquipment(
                EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID-считыватель");
        FunctionalConfigurationDTO lbcFunctional = lbcALS.getLBC().getFunctionalConfiguration();
        lbcFunctional.setDisplay(DisplayPurpose.INCH_10);
        lbcFunctional.setAccess(Set.of(AccessPurpose.RFID, AccessPurpose.BAR_CODE));
        lbcFunctional.setPayment(PaymentPurpose.NONE);
        lbcFunctional.setPrinter(PrinterPurpose.ABSENT);
        lbcALS.getLBC().setSelectedEquipment(List.of(
                TestDataFactory.selectedEquipment(display.getId(), 1),
                TestDataFactory.selectedEquipment(barcode.getId(), 2),
                TestDataFactory.selectedEquipment(rfid.getId(), 1)));
        lbcALS.getLBC().setEquipmentProvided(true);

        ALS domainALS = restoreAlsDto(lbcALS);
        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();

        ALSEntity alsEntity = ALSEntityMapper.toEntity(
                domainALS,
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate, equipment -> equipmentRepository.findById(equipment.getId()).orElseThrow())
                        )
                )
        );
        Long alsId = alsRepository.saveAndFlush(alsEntity).getId();

        entityManager.clear();

        ALSEntity restoredEntity = alsRepository.findById(alsId).orElseThrow();
        ALS restoredALS = restoreAlsEntity(restoredEntity);
        LBCEntity restoredLBC = (LBCEntity) Hibernate.unproxy(
                restoredEntity.getModules().get(0).getModule()
        );

        assertEquals(domainALS, restoredALS);
        assertEquals(3, restoredLBC.getEquipment().size());
        assertEquals(
                2,
                restoredLBC.getEquipment().stream()
                        .filter(item -> item.getEquipment().getId().equals(barcode.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getQuantity()
        );
        assertEquals(Set.of(AccessPurpose.RFID, AccessPurpose.BAR_CODE),
                restoredLBC.getAccessPurposes());
        assertEquals(PaymentPurpose.NONE,
                restoredLBC.getPaymentPurpose());
    }

    @Test
    void replacingOneAlsInProject_shouldPreserveOtherInstancesAndConfigurations() {
        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setFirstName("Тест");
        employeeEntity.setLastName("ALS replacement");
        employeeEntity.setEmail("als-replacement@example.test");
        employeeEntity.setEncryptedPassword("encoded-password");
        employeeEntity.setRegistrationDate(LocalDate.of(2026, 1, 10));
        employeeEntity.setRole(Role.ROLE_ADMIN);
        employeeEntity = employeeRepository.saveAndFlush(employeeEntity);

        ALS originalALS = TestDataFactory.validALS(null);
        ALS otherALS = TestDataFactory.restoreALS(TestDataFactory.validLBCALSDTO(null));

        ALSDTO changedDTO = TestDataFactory.validALSDTO(null);
        changedDTO.getLbList().get(1).setCountCells(4);
        ALS changedALS = TestDataFactory.restoreALS(changedDTO);

        Map<Module, ModuleEntity> resolvedModules = new HashMap<>();
        java.util.function.Function<Module, ModuleEntity> moduleResolver =
                module -> resolvedModules.computeIfAbsent(
                        module,
                        candidate -> moduleRepository.saveAndFlush(
                                ModuleEntityMapper.toEntity(candidate, equipment -> equipmentRepository.findById(equipment.getId()).orElseThrow())
                        )
                );

        ALSEntity originalEntity = alsRepository.saveAndFlush(
                ALSEntityMapper.toEntity(originalALS, moduleResolver)
        );
        ALSEntity otherEntity = alsRepository.saveAndFlush(
                ALSEntityMapper.toEntity(otherALS, moduleResolver)
        );

        Project domainProject = new Project(
                "ALS replacement",
                "Test company",
                LocalDate.of(2026, 1, 10),
                EmployeeEntityMapper.toDomain(employeeEntity)
        );
        domainProject.addALS(originalALS);
        domainProject.addALS(originalALS);
        domainProject.addALS(otherALS);

        ProjectEntity projectEntity = ProjectEntityMapper.toEntity(
                domainProject,
                employee -> employeeRepository.findById(employee.getId())
                        .orElseThrow(),
                als -> als.equals(originalALS) ? originalEntity : otherEntity
        );
        Long projectId = projectRepository.saveAndFlush(projectEntity).getId();
        Long originalAlsId = originalEntity.getId();
        Long otherAlsId = otherEntity.getId();

        ALSEntity changedEntity = alsRepository.saveAndFlush(
                ALSEntityMapper.toEntity(changedALS, moduleResolver)
        );
        Long changedAlsId = changedEntity.getId();

        entityManager.clear();

        ProjectEntity managedProjectEntity =
                projectRepository.findById(projectId).orElseThrow();
        Project restoredProject = restoreProjectEntity(managedProjectEntity);
        restoredProject.replaceALS(originalALS, changedALS);

        Map<Long, ALSEntity> persistedConfigurations = Map.of(
                originalAlsId, alsRepository.findById(originalAlsId).orElseThrow(),
                otherAlsId, alsRepository.findById(otherAlsId).orElseThrow(),
                changedAlsId, alsRepository.findById(changedAlsId).orElseThrow()
        );

        ProjectEntityMapper.updateEntity(
                restoredProject,
                managedProjectEntity,
                employee -> employeeRepository.findById(employee.getId())
                        .orElseThrow(),
                als -> {
                    if (als.equals(originalALS)) {
                        return persistedConfigurations.get(originalAlsId);
                    }
                    if (als.equals(otherALS)) {
                        return persistedConfigurations.get(otherAlsId);
                    }
                    if (als.equals(changedALS)) {
                        return persistedConfigurations.get(changedAlsId);
                    }
                    throw new IllegalStateException("Неизвестная конфигурация ALS");
                }
        );
        projectRepository.saveAndFlush(managedProjectEntity);
        entityManager.clear();

        ProjectEntity restoredEntity =
                projectRepository.findById(projectId).orElseThrow();
        Project finalProject = restoreProjectEntity(restoredEntity);

        assertEquals(1, finalProject.getQuantity(originalALS));
        assertEquals(1, finalProject.getQuantity(changedALS));
        assertEquals(1, finalProject.getQuantity(otherALS));
        assertEquals(3, restoredEntity.getAlsEntries().size());
        assertTrue(restoredEntity.getAlsEntries().stream()
                .anyMatch(entry -> entry.getAls().getId().equals(originalAlsId)));
        assertTrue(restoredEntity.getAlsEntries().stream()
                .anyMatch(entry -> entry.getAls().getId().equals(changedAlsId)));
        assertTrue(restoredEntity.getAlsEntries().stream()
                .anyMatch(entry -> entry.getAls().getId().equals(otherAlsId)));

        ALSEntity restoredOriginalEntity =
                alsRepository.findById(originalAlsId).orElseThrow();
        ALSEntity restoredChangedEntity =
                alsRepository.findById(changedAlsId).orElseThrow();

        Set<Long> originalModuleIds = restoredOriginalEntity.getModules().stream()
                .map(association -> association.getModule().getId())
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> changedModuleIds = restoredChangedEntity.getModules().stream()
                .map(association -> association.getModule().getId())
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                2,
                originalModuleIds.stream()
                        .filter(changedModuleIds::contains)
                        .count()
        );

        ALS restoredChangedALS =
                restoreAlsEntity(restoredChangedEntity);
        assertEquals(
                List.of("LEFT", "RIGHT"),
                restoredChangedALS.getStorageModules().stream()
                        .map(storageModule ->
                                storageModule.getDirectionDoorOpening().name())
                        .toList()
        );
        assertEquals(
                List.of(1, 2, 3, 4, 5, 6, 7),
                restoredChangedALS.getStorageModules().stream()
                        .flatMap(storageModule ->
                                storageModule.getCells().stream())
                        .map(cell -> cell.getNumber())
                        .toList()
        );
    }


    @Test
    void projectService_shouldReplaceOnlySelectedAlsAndPreserveOtherConfigurations() {
        EmployeeEntity employeeEntity = new EmployeeEntity();
        employeeEntity.setFirstName("Тест");
        employeeEntity.setLastName("Project owner");
        employeeEntity.setEmail("project-owner@example.test");
        employeeEntity.setEncryptedPassword("encoded-password");
        employeeEntity.setRegistrationDate(LocalDate.of(2026, 1, 10));
        employeeEntity.setRole(Role.ROLE_ADMIN);
        employeeEntity = employeeRepository.saveAndFlush(employeeEntity);

        EmployeePrincipal principal = new EmployeePrincipal(
                employeeEntity.getId(),
                employeeEntity.getEmail(),
                employeeEntity.getEncryptedPassword(),
                employeeEntity.getRole(),
                employeeEntity.isActive()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );

        try {
            Employee employee = EmployeeEntityMapper.toDomain(employeeEntity);
            ALS repeatedALS = restoreAlsDto(TestDataFactory.validALSDTO(null));
            ALS independentALS = restoreAlsDto(TestDataFactory.validLBCALSDTO(null));

            Project requested = new Project(
                    "ProjectService replacement",
                    "Test company",
                    LocalDate.of(2026, 1, 10),
                    employee
            );
            requested.addALS(repeatedALS);
            requested.addALS(repeatedALS);
            requested.addALS(independentALS);

            Project savedProject = projectService.saveProject(requested);
            List<ALS> savedConfigurations =
                    new ArrayList<>(savedProject.getQuantityALS().keySet());
            ALS originalConfiguration = savedConfigurations.get(0).copy();
            ALS independentConfiguration = savedConfigurations.get(1).copy();
            Long projectId = savedProject.getId();
            Long selectedAlsId = originalConfiguration.getId();
            Long independentAlsId = independentConfiguration.getId();
            LocalDate originalCreatedAt = savedProject.getCreatedAt();

            EmployeeEntity editorEntity = new EmployeeEntity();
            editorEntity.setFirstName("Тест");
            editorEntity.setLastName("Project editor");
            editorEntity.setEmail("project-editor@example.test");
            editorEntity.setEncryptedPassword("encoded-password");
            editorEntity.setRegistrationDate(LocalDate.of(2026, 1, 10));
            editorEntity.setRole(Role.ROLE_ADMIN);
            editorEntity = employeeRepository.saveAndFlush(editorEntity);

            EmployeePrincipal editorPrincipal = new EmployeePrincipal(
                    editorEntity.getId(),
                    editorEntity.getEmail(),
                    editorEntity.getEncryptedPassword(),
                    editorEntity.getRole(),
                    editorEntity.isActive()
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            editorPrincipal,
                            null,
                            editorPrincipal.getAuthorities()
                    )
            );

            Project tamperedProject = Project.restore(
                    savedProject.getName(),
                    savedProject.getCompany(),
                    LocalDate.of(2000, 1, 1),
                    EmployeeEntityMapper.toDomain(editorEntity),
                    LocalDate.of(2000, 1, 1),
                    employee,
                    savedProject.getQuantityALS()
            );
            tamperedProject.setId(projectId);
            Project updatedMetadata = projectService.saveProject(tamperedProject);

            assertEquals(employeeEntity.getId(), updatedMetadata.getCreatedBy().getId());
            assertEquals(originalCreatedAt, updatedMetadata.getCreatedAt());
            assertEquals(editorEntity.getId(), updatedMetadata.getUpdatedBy().getId());
            assertEquals(LocalDate.now(), updatedMetadata.getUpdatedAt());

            ALS changedALS = projectService.addLBAtProject(projectId, selectedAlsId);
            LB changedLB = changedALS.getStorageModules().stream()
                    .filter(LB.class::isInstance)
                    .map(LB.class::cast)
                    .findFirst()
                    .orElseThrow();
            LBDTO changedLBDTO = LBDtoMapper.toDto(changedLB);
            changedLBDTO.setCountCells(4);
            changedALS = projectService.saveLBAtProject(
                    projectId,
                    changedALS.getId(),
                    changedLB.getId(),
                    LBDtoMapper.toDomain(changedLBDTO, new TypeLb(changedLBDTO.getType(), changedLBDTO.getDeltaWidth(), changedLBDTO.getShelfThick(), changedLBDTO.getServiceZoneWidth()))
            );

            Long removedLBId = changedALS.getStorageModules().stream()
                    .filter(LB.class::isInstance)
                    .map(LB.class::cast)
                    .skip(1)
                    .map(LB::getId)
                    .findFirst()
                    .orElseThrow();
            changedALS = projectService.deleteLBatProject(
                    projectId,
                    changedALS.getId(),
                    removedLBId
            );
            assertEquals(2, changedALS.getStorageModules().size());

            LCDTO changedLCDTO = LCDtoMapper.toDto(changedALS.getLC());
            Equipment barcodeScanner = persistEquipment(
                    EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                    EquipmentCategory.BAR_READER, "Сканер штрихкода для LC");
            changedLCDTO.getFunctionalConfiguration().setAccess(Set.of(AccessPurpose.BAR_CODE));
            changedLCDTO.setSelectedEquipment(List.of(TestDataFactory.selectedEquipment(barcodeScanner.getId(), 1)));
            changedLCDTO.setEquipmentProvided(true);
            ALS changedWithLC = projectService.saveLCAtProject(
                    projectId,
                    changedALS.getId(),
                    changedLCDTO.getId(),
                    LCDtoMapper.toDomain(changedLCDTO, equipmentCatalogService::findById, changedALS.getLC().getControlConfiguration(), TestDataFactory.createSizeBounds())
            );
            var selectedLCEquipment = LCDtoMapper.toDto(changedWithLC.getLC()).getSelectedEquipment();
            assertEquals(1, selectedLCEquipment.size());
            assertEquals(barcodeScanner.getId(), selectedLCEquipment.get(0).getId());
            assertEquals(1, selectedLCEquipment.get(0).getQuantity());

            LBCDTO changedLBCDTO = LBCDtoMapper.toDto(independentConfiguration.getLBC());
            changedLBCDTO.getFunctionalConfiguration().setAccess(Set.of(AccessPurpose.BAR_CODE));
            changedLBCDTO.setSelectedEquipment(List.of(TestDataFactory.selectedEquipment(barcodeScanner.getId(), 1)));
            changedLBCDTO.setEquipmentProvided(true);
            ALS changedWithLBC = projectService.saveLBCAtProject(
                    projectId,
                    independentAlsId,
                    changedLBCDTO.getId(),
                    LBCDtoMapper.toDomain(changedLBCDTO, new TypeLb(changedLBCDTO.getType(), changedLBCDTO.getDeltaWidth(), changedLBCDTO.getShelfThick(), changedLBCDTO.getServiceZoneWidth()), equipmentCatalogService::findById, independentConfiguration.getLBC().getControlConfiguration(), TestDataFactory.createSizeBounds())
            );
            assertEquals(
                    Set.of(AccessPurpose.BAR_CODE),
                    LBCDtoMapper.toDto(changedWithLBC.getLBC())
                            .getFunctionalConfiguration().getAccess()
            );

            ALS regeneratedLBC = projectService.replaceWithNewLBCAtProject(
                    projectId,
                    changedWithLBC.getId()
            );
            // Идентичная конфигурация LBC переиспользуется глобальным Find-or-Create.
            assertEquals(changedWithLBC.getLBC().getId(), regeneratedLBC.getLBC().getId());
            assertNotEquals(independentAlsId, regeneratedLBC.getId());

            entityManager.flush();
            entityManager.clear();

            ProjectEntity restoredEntity =
                    projectRepository.findById(projectId).orElseThrow();
            assertEquals(employeeEntity.getId(), restoredEntity.getCreatedBy().getId());
            assertEquals(editorEntity.getId(), restoredEntity.getUpdatedBy().getId());
            assertEquals(originalCreatedAt, restoredEntity.getCreatedAt());
            assertEquals(LocalDate.now(), restoredEntity.getUpdatedAt());

            Project restoredProject = restoreProjectEntity(restoredEntity);

            assertEquals(1, restoredProject.getQuantity(originalConfiguration));
            assertEquals(0, restoredProject.getQuantity(independentConfiguration));
            assertEquals(1, restoredProject.getQuantity(changedWithLC));
            assertEquals(1, restoredProject.getQuantity(regeneratedLBC));
            assertEquals(3, restoredEntity.getAlsEntries().size());

            ALSEntity restoredChangedLCEntity =
                    alsRepository.findById(changedWithLC.getId()).orElseThrow();
            ALS restoredChangedLC =
                    restoreAlsEntity(restoredChangedLCEntity);
            assertEquals(2, restoredChangedLC.getStorageModules().size());
            assertEquals(
                    List.of(1, 2, 3, 4, 5, 6, 7),
                    restoredChangedLC.getStorageModules().stream()
                            .flatMap(storageModule -> storageModule.getCells().stream())
                            .map(cell -> cell.getNumber())
                            .toList()
            );

            ProjectDTO restoredDTO =
                    ProjectDtoMapper.toDto(projectService.findById(projectId));
            ALSDTO persistedLCConfiguration = restoredDTO.getAlsList().stream()
                    .filter(value -> value.getId().equals(changedWithLC.getId()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1, persistedLCConfiguration.getLC().getSelectedEquipment().size());
            assertEquals(barcodeScanner.getId(),
                    persistedLCConfiguration.getLC().getSelectedEquipment().get(0).getId());
            assertEquals(1,
                    persistedLCConfiguration.getLC().getSelectedEquipment().get(0).getQuantity());

            ALSDTO persistedLBCConfiguration = restoredDTO.getAlsList().stream()
                    .filter(value -> value.getId().equals(regeneratedLBC.getId()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(
                    LBCDtoMapper.toDto(regeneratedLBC.getLBC())
                            .getFunctionalConfiguration().getAccess(),
                    persistedLBCConfiguration.getLBC()
                            .getFunctionalConfiguration().getAccess()
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
    private Equipment persistEquipment(
            EquipmentFunctionalType functionalType,
            com.lb_calc_web.domain.equipment.EquipmentPurpose purpose,
            EquipmentCategory category,
            String name
    ) {
        Equipment domain = new Equipment(
                null, functionalType, purpose, category, name, "",
                120, 50, 30
        );
        EquipmentEntity saved = equipmentRepository.saveAndFlush(
                EquipmentEntityMapper.toEntity(domain));
        return EquipmentEntityMapper.toDomain(saved);
    }

    private ALS restoreAlsEntity(ALSEntity entity) {
        return ALSEntityMapper.toDomain(entity, TestDataFactory.createSizeBounds());
    }

    private Project restoreProjectEntity(ProjectEntity entity) {
        return ProjectEntityMapper.toDomain(
                entity,
                this::restoreAlsEntity,
                (als, alsEntity) -> { }
        );
    }

    private ALS restoreAlsDto(ALSDTO dto) {
        return ALSDtoMapper.toDomain(
                dto,
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                equipmentCatalogService::findById,
                null,
                TestDataFactory.createSizeBounds()
        );
    }

}
