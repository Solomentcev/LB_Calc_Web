package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.validation.ValidationSizeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LCServiceTest {

    private FindOrCreateLockService lockService;
    private LCRepository repository;
    private EquipmentRepository equipmentRepository;
    private EquipmentCatalogService equipmentCatalogService;
    private LCService service;

    @BeforeEach
    void setUp() {
        lockService = mock(FindOrCreateLockService.class);
        repository = mock(LCRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);
        doAnswer(invocation -> {
            ControlConfiguration configuration = invocation.getArgument(0);
            ControlConfiguration fallback = invocation.getArgument(1);
            boolean selectionProvided = invocation.getArgument(2);
            boolean functionalChanged = fallback != null
                    && !configuration.getFunctionalConfiguration()
                    .equals(fallback.getFunctionalConfiguration());
            if (!selectionProvided && fallback != null) {
                configuration = new ControlConfiguration(
                        configuration.getFunctionalConfiguration(), fallback.getEquipment());
            }
            if (!selectionProvided || functionalChanged) {
                equipmentCatalogService.configureFunctionalEquipment(configuration);
            }
            return configuration;
        }).when(equipmentCatalogService).prepareFunctionalEquipment(
                any(ControlConfiguration.class), nullable(ControlConfiguration.class), anyBoolean());
        service = new LCService(
                repository, lockService,
                TestDataFactory.createSizeValidator(), equipmentRepository, TestDataFactory.createSizeBounds(),
                equipmentCatalogService);
    }

    @Test
    void createLC_shouldBuildFunctionalConfigurationAndSelectDefaultEquipment() {
        Equipment display = new Equipment(
                701L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10", "", 300, 180, 30);
        Equipment rfid = new Equipment(
                702L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        doAnswer(invocation -> {
            ControlConfiguration configuration = invocation.getArgument(0);
            configuration.configureEquipment(
                    configuration.getFunctionalConfiguration(),
                    List.of(display, rfid),
                    Set.of(display, rfid));
            return null;
        }).when(equipmentCatalogService).configureFunctionalEquipment(any(ControlConfiguration.class));

        LC domain = service.createLC();
        var functional = domain.getControlConfiguration().getFunctionalConfiguration();

        assertEquals(DisplayPurpose.INCH_10, functional.getDisplay());
        assertEquals(Set.of(AccessPurpose.RFID), functional.getAccess());
        assertEquals(Map.of(display, 1, rfid, 1), domain.getEquipment());
        assertEquals(350, domain.getWidth(),
                "Ширина LC должна рассчитываться с учётом габаритов выбранного дисплея");
        verify(equipmentCatalogService).configureFunctionalEquipment(domain.getControlConfiguration());
    }

    @Test
    void restoreDomain_shouldResolveEquipmentByLongId() throws Exception {
        Equipment equipment = new Equipment(
                71L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        EquipmentEntity equipmentEntity = EquipmentEntityMapper.toEntity(equipment);
        Field id = EquipmentEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(equipmentEntity, 71L);
        when(equipmentRepository.findById(71L)).thenReturn(Optional.of(equipmentEntity));
        LCDTO dto = TestDataFactory.validLCDTO(null);
        dto.setSelectedEquipment(List.of(TestDataFactory.selectedEquipment(71L, 2)));
        dto.setEquipmentProvided(true);

        LC restored = LCDtoMapper.toDomain(dto, idValue -> {
            assertEquals(71L, idValue);
            return equipment;
        }, TestDataFactory.createSizeBounds());

        assertEquals(2, restored.getControlConfiguration().getQuantity(equipment));
    }

    @Test
    void mapper_shouldNotRestoreEquipmentWhenSelectionListIsOmitted() {
        LCDTO dto = TestDataFactory.validLCDTO(17L);
        dto.setEquipmentProvided(false);

        LC restored = LCDtoMapper.toDomain(dto, idValue -> {
            fail("Для отсутствующей комплектации поиск оборудования не требуется");
            return null;
        }, TestDataFactory.createSizeBounds());

        assertTrue(restored.getEquipment().isEmpty());
        verifyNoInteractions(equipmentRepository);
    }

    @Test
    void mapper_shouldTreatMissingAccessInProvidedFunctionalConfigurationAsEmpty() {
        LCDTO dto = TestDataFactory.validLCDTO(20L);
        var functional = new com.lb_calc_web.dto.FunctionalConfigurationDTO();
        functional.setDisplay(com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_19);
        functional.setPayment(com.lb_calc_web.domain.equipment.PaymentPurpose.NONE);
        functional.setPrinter(com.lb_calc_web.domain.equipment.PrinterPurpose.ABSENT);
        dto.setFunctionalConfiguration(functional);

        LC restored = LCDtoMapper.toDomain(dto, idValue -> {
            fail("Для отсутствующей комплектации поиск оборудования не требуется");
            return null;
        }, TestDataFactory.createSizeBounds());

        assertTrue(restored.getControlConfiguration().getFunctionalConfiguration()
                .getAccess().isEmpty());
        assertTrue(restored.getEquipment().isEmpty());
    }

    @Test
    void mapper_shouldUseDomainDefaultsWhenNestedFunctionalDtoIsEmpty() {
        LCDTO dto = TestDataFactory.validLCDTO(19L);
        dto.setFunctionalConfiguration(new com.lb_calc_web.dto.FunctionalConfigurationDTO());

        LC restored = LCDtoMapper.toDomain(dto, idValue -> {
            fail("Для отсутствующей комплектации поиск оборудования не требуется");
            return null;
        }, TestDataFactory.createSizeBounds());

        assertEquals(DisplayPurpose.NONE,
                restored.getControlConfiguration().getFunctionalConfiguration().getDisplay());
        assertTrue(restored.getControlConfiguration().getFunctionalConfiguration().getAccess().isEmpty());
        assertTrue(restored.getEquipment().isEmpty());
    }

    @Test
    void mapper_shouldKeepExplicitlyEmptySelectionEmpty() {
        LCDTO dto = TestDataFactory.validLCDTO(18L);
        dto.setSelectedEquipment(List.of());
        dto.setEquipmentProvided(true);

        LC restored = LCDtoMapper.toDomain(dto, idValue -> {
            fail("Для пустого выбора каталог вызываться не должен");
            return null;
        }, TestDataFactory.createSizeBounds());

        assertTrue(restored.getControlConfiguration().getEquipment().isEmpty());
    }

    @Test
    void findById_shouldReturnMappedDomain() {
        LCEntity entity = spy(LCEntityMapper.toEntity(
                TestDataFactory.validLC(null), equipment -> null));
        doReturn(25L).when(entity).getId();
        when(repository.findById(25L)).thenReturn(Optional.of(entity));

        LC result = service.findById(25L);

        assertEquals(25L, result.getId());
    }

    @Test
    void findById_shouldRejectUnknownId() {
        when(repository.findById(25L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.findById(25L));
    }

    @Test
    void prepareFunctionalEquipment_shouldApplySelectionToModuleAndRecalculateWidth() {
        LC domain = TestDataFactory.validLC(null);
        ControlConfiguration configuration = domain.getControlConfiguration();
        var functional = configuration.getFunctionalConfiguration();
        configuration.setFunctionalConfiguration(new com.lb_calc_web.domain.model.FunctionalConfiguration(
                DisplayPurpose.INCH_17, functional.getAccess(), functional.getPayment(), functional.getPrinter()));
        domain.setControlConfiguration(configuration);

        Equipment display = new Equipment(
                799L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_17,
                EquipmentCategory.DISPLAY, "Дисплей 17", "", 400, 250, 30);
        doAnswer(invocation -> {
            ControlConfiguration selected = invocation.getArgument(0);
            selected.setEquipment(Map.of(display, 1));
            return null;
        }).when(equipmentCatalogService).configureFunctionalEquipment(any(ControlConfiguration.class));

        service.prepareFunctionalEquipment(domain, null, false);

        assertEquals(Map.of(display, 1), domain.getEquipment());
        assertEquals(450, domain.getWidth(),
                "Ширина LC должна пересчитываться по новой физической модели дисплея");
    }

    @Test
    void configureWithoutDisplay_shouldRestoreMinimumWidth() {
        LC domain = TestDataFactory.validLC(null);
        Equipment display = new Equipment(
                7992L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_17,
                EquipmentCategory.DISPLAY, "Дисплей 17", "", 400, 250, 30);

        ControlConfiguration configuration = domain.getControlConfiguration();
        configuration.setEquipment(Map.of(display, 1));
        domain.setControlConfiguration(configuration);
        assertEquals(450, domain.getWidth());

        configuration = domain.getControlConfiguration();
        configuration.setEquipment(Map.of());
        domain.setControlConfiguration(configuration);

        assertEquals(200, domain.getWidth(),
                "После удаления дисплея ширина LC должна вернуться к минимуму");
    }

    @Test
    void prepareFunctionalEquipment_shouldRestoreCompatibleFallbackWhenSelectionWasNotProvided() {
        LC domain = TestDataFactory.validLC(41L);
        Equipment scanner = new Equipment(
                704L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        ControlConfiguration fallback = new ControlConfiguration(
                domain.getControlConfiguration().getFunctionalConfiguration(),
                Map.of(scanner, 2));

        service.prepareFunctionalEquipment(domain, fallback, false);

        assertEquals(Map.of(scanner, 2), domain.getEquipment());
        verify(equipmentCatalogService).configureFunctionalEquipment(any(ControlConfiguration.class));
    }


    @Test
    void prepareFunctionalEquipment_shouldPreserveExplicitEmptySelectionWhenFunctionsAreUnchanged() {
        LC domain = TestDataFactory.validLC(null);
        ControlConfiguration fallback = domain.getControlConfiguration();

        service.prepareFunctionalEquipment(domain, fallback, true);

        verify(equipmentCatalogService, never()).configureFunctionalEquipment(any());
    }

    @Test
    void prepareFunctionalEquipment_shouldResolveEquipmentWhenFunctionsChange() {
        LC domain = TestDataFactory.validLC(null);
        ControlConfiguration fallback = domain.getControlConfiguration();
        ControlConfiguration updated = domain.getControlConfiguration();
        var current = updated.getFunctionalConfiguration();
        updated.setFunctionalConfiguration(new com.lb_calc_web.domain.model.FunctionalConfiguration(
                DisplayPurpose.INCH_17, current.getAccess(), current.getPayment(), current.getPrinter()));
        domain.setControlConfiguration(updated);

        service.prepareFunctionalEquipment(domain, fallback, true);

        verify(equipmentCatalogService).configureFunctionalEquipment(any());
    }


    private List<Equipment> configureValidEquipment(LC domain) {
        Equipment display = new Equipment(
                7101L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10", "", 300, 180, 30);
        Equipment rfid = new Equipment(
                7102L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID-считыватель", "", 100, 50, 30);
        ControlConfiguration configuration = domain.getControlConfiguration();
        configuration.setEquipment(Map.of(display, 1, rfid, 1));
        domain.setControlConfiguration(configuration);
        return List.of(display, rfid);
    }

    private void stubEquipment(List<Equipment> models) throws Exception {
        Field idField = EquipmentEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        for (Equipment model : models) {
            EquipmentEntity entity = EquipmentEntityMapper.toEntity(model);
            idField.set(entity, model.getId());
            when(equipmentRepository.findById(model.getId())).thenReturn(Optional.of(entity));
        }
    }

    @Test
    void saveLC_shouldPersistUsingExistingEquipmentReferences() throws Exception {
        LC domain = TestDataFactory.validLC(null);
        List<Equipment> equipment = configureValidEquipment(domain);
        stubEquipment(equipment);
        LCEntity saved = spy(LCEntityMapper.toEntity(domain, equipmentModel -> equipmentRepository.findById(equipmentModel.getId()).orElseThrow()));
        doReturn(99L).when(saved).getId();
        when(repository.findCandidates(anyInt(), anyInt(), anyInt(), anyInt(),
                anyInt(), any(), any())).thenReturn(List.of());
        when(repository.save(any(LCEntity.class))).thenReturn(saved);

        LC result = service.saveLC(domain);

        assertEquals(99L, result.getId());
        verify(lockService).acquire();
        verify(repository).save(any(LCEntity.class));
    }

    @Test
    void saveLC_shouldRejectInvalidGeometryBeforeSaving() {
        LC source = TestDataFactory.validLC(null);
        LC domain = new LC(
                10, source.getWidth(), source.getDepth(), source.getUpperFrame(),
                source.getBottomFrame(), source.getColorBody(), source.getColorDoor(),
                source.getControlConfiguration(), TestDataFactory.createSizeBounds());
        configureValidEquipment(domain);

        assertThrows(ValidationSizeException.class, () -> service.saveLC(domain));
        verify(repository, never()).save(any());
    }
}
