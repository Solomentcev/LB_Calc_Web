package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.validation.ValidationSizeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

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
    private MockEnvironment environment;
    private LCService service;

    @BeforeEach
    void setUp() {
        lockService = mock(FindOrCreateLockService.class);
        repository = mock(LCRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.width.default", "500")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("lc.payment.default", "NONE")
                .withProperty("lc.printer.default", "false")
                .withProperty("lc.access-purposes.default", "RFID")
                .withProperty("lc.color.body.default", "Blue")
                .withProperty("lc.color.door.default", "White");
        service = new LCService(
                repository, lockService, environment,
                TestDataFactory.createSizeValidator(), equipmentRepository);
    }

    @Test
    void createLC_shouldBuildFunctionalConfigurationWithoutImplicitHardware() {
        LC domain = service.createLC();
        var functional = domain.getControlConfiguration().getFunctionalConfiguration();

        assertEquals(DisplayPurpose.INCH_10, functional.getDisplay());
        assertEquals(Set.of(AccessPurpose.RFID), functional.getAccess());
        assertEquals(0, domain.getEquipment().size());
        assertEquals(500, domain.getWidth());
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
        dto.setEquipmentSelections(List.of(new EquipmentSelectionDTO(71L, 2)));

        LC restored = service.restoreDomain(dto);

        assertEquals(2, restored.getControlConfiguration().getQuantity(equipment));
        verify(equipmentRepository).findById(71L);
    }

    @Test
    void restoreDomain_shouldRetainEquipmentWhenSelectionListIsOmitted() {
        Equipment equipment = new Equipment(
                73L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        ControlConfiguration fallback = new ControlConfiguration(
                TestDataFactory.validLC(null).getControlConfiguration().getFunctionalConfiguration(),
                Map.of(equipment, 2)
        );
        LCDTO dto = TestDataFactory.validLCDTO(17L);
        dto.setFunctionalConfiguration(null);
        dto.setEquipmentSelections(null);

        LC restored = service.restoreDomain(dto, fallback);

        assertEquals(fallback, restored.getControlConfiguration());
        verifyNoInteractions(equipmentRepository);
    }

    @Test
    void restoreDomain_shouldPreserveFunctionalConfigurationWhenNestedDtoIsEmpty() {
        Equipment equipment = new Equipment(
                75L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        ControlConfiguration fallback = new ControlConfiguration(
                TestDataFactory.validLC(null).getControlConfiguration().getFunctionalConfiguration(),
                Map.of(equipment, 1)
        );
        LCDTO dto = TestDataFactory.validLCDTO(19L);
        dto.setFunctionalConfiguration(new com.lb_calc_web.dto.FunctionalConfigurationDTO());
        dto.setEquipmentSelections(null);

        LC restored = service.restoreDomain(dto, fallback);

        assertEquals(fallback.getFunctionalConfiguration(),
                restored.getControlConfiguration().getFunctionalConfiguration());
        assertEquals(fallback.getEquipment(), restored.getEquipment());
    }

    @Test
    void restoreDomain_shouldClearEquipmentWhenEmptySelectionListIsExplicit() {
        Equipment equipment = new Equipment(
                74L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID,
                EquipmentCategory.RFID_READER, "RFID", "", 100, 50, 30);
        ControlConfiguration fallback = new ControlConfiguration(
                TestDataFactory.validLC(null).getControlConfiguration().getFunctionalConfiguration(),
                Map.of(equipment, 1)
        );
        LCDTO dto = TestDataFactory.validLCDTO(18L);
        dto.setEquipmentSelections(List.of());

        LC restored = service.restoreDomain(dto, fallback);

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
    void saveLC_shouldPersistUsingExistingEquipmentReferences() {
        LC domain = TestDataFactory.validLC(null);
        LCEntity saved = spy(LCEntityMapper.toEntity(domain, equipment -> null));
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
                source.getControlConfiguration());

        assertThrows(ValidationSizeException.class, () -> service.saveLC(domain));
        verify(repository, never()).save(any());
    }
}
