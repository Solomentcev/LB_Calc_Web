package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
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

class LBCServiceTest {

    private FindOrCreateLockService lockService;
    private LBCRepository repository;
    private EquipmentRepository equipmentRepository;
    private EquipmentCatalogService equipmentCatalogService;
    private MockEnvironment environment;
    private LBCService service;

    @BeforeEach
    void setUp() {
        lockService = mock(FindOrCreateLockService.class);
        repository = mock(LBCRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);
        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.width.default", "500")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("size.count.cells.default", "3")
                .withProperty("size.door.thickness.default", "20")
                .withProperty("lbc.type.default", "TYPE1")
                .withProperty("lbc.payment.default", "NONE")
                .withProperty("lbc.printer.default", "false")
                .withProperty("lbc.access-purposes.default", "RFID")
                .withProperty("lbc.color.body.default", "Blue")
                .withProperty("lbc.color.door.default", "White")
                .withProperty("lbc.direction.default", "LEFT")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");
        service = new LBCService(
                repository, lockService, environment,
                TestDataFactory.createSizeValidator(), equipmentRepository, TestDataFactory.createSizeBounds(),
                equipmentCatalogService);
    }

    @Test
    void createLBC_shouldBuildFunctionalConfigurationAndSelectDefaultEquipment() {
        var display = new com.lb_calc_web.domain.equipment.Equipment(
                7101L, com.lb_calc_web.domain.equipment.EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10, com.lb_calc_web.domain.equipment.EquipmentCategory.DISPLAY,
                "Дисплей 10", "", 300, 180, 30);
        var rfid = new com.lb_calc_web.domain.equipment.Equipment(
                7102L, com.lb_calc_web.domain.equipment.EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID, com.lb_calc_web.domain.equipment.EquipmentCategory.RFID_READER,
                "RFID", "", 100, 50, 30);
        doAnswer(invocation -> {
            var configuration = invocation.<com.lb_calc_web.domain.model.ControlConfiguration>getArgument(0);
            configuration.configureEquipment(
                    configuration.getFunctionalConfiguration(),
                    List.of(display, rfid),
                    Set.of(display, rfid));
            return null;
        }).when(equipmentCatalogService).configureFunctionalEquipment(
                any(com.lb_calc_web.domain.model.ControlConfiguration.class));

        LBC domain = service.createLBC();
        var functional = domain.getControlConfiguration().getFunctionalConfiguration();

        assertEquals(DisplayPurpose.INCH_10, functional.getDisplay());
        assertEquals(Set.of(AccessPurpose.RFID), functional.getAccess());
        assertEquals(Map.of(display, 1, rfid, 1), domain.getEquipment());
        assertEquals(500, domain.getWidth());
        verify(equipmentCatalogService).configureFunctionalEquipment(domain.getControlConfiguration());
        assertEquals(3, domain.getCountCells());
    }

    @Test
    void restoreDomain_shouldResolveEquipmentByLongId() throws Exception {
        var equipment = new com.lb_calc_web.domain.equipment.Equipment(
                72L,
                com.lb_calc_web.domain.equipment.EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID,
                com.lb_calc_web.domain.equipment.EquipmentCategory.RFID_READER,
                "RFID", "", 100, 50, 30);
        EquipmentEntity equipmentEntity = EquipmentEntityMapper.toEntity(equipment);
        Field id = EquipmentEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(equipmentEntity, 72L);
        when(equipmentRepository.findById(72L)).thenReturn(Optional.of(equipmentEntity));
        LBCDTO dto = TestDataFactory.validLBCDTO(null);
        dto.setSelectedEquipment(List.of(TestDataFactory.selectedEquipment(72L, 3)));
        dto.setEquipmentProvided(true);

        LBC restored = LBCDtoMapper.toDomain(
                dto, TestDataFactory.validLBC(null).getTypeLb(), idValue -> {
                    assertEquals(72L, idValue);
                    return equipment;
                }, null, TestDataFactory.createSizeBounds());

        assertEquals(3, restored.getControlConfiguration().getQuantity(equipment));
    }

    @Test
    void configureFunctionalEquipment_shouldApplySelectedModelsToLbc() {
        LBC domain = TestDataFactory.validLBC(null);
        var functional = domain.getControlConfiguration().getFunctionalConfiguration();
        var display = new com.lb_calc_web.domain.equipment.Equipment(
                7991L, com.lb_calc_web.domain.equipment.EquipmentFunctionalType.DISPLAY,
                functional.getDisplay(), com.lb_calc_web.domain.equipment.EquipmentCategory.DISPLAY,
                "Дисплей", "", 300, 180, 30);
        doAnswer(invocation -> {
            var configuration = invocation.<com.lb_calc_web.domain.model.ControlConfiguration>getArgument(0);
            configuration.setEquipment(Map.of(display, 1));
            return null;
        }).when(equipmentCatalogService).configureFunctionalEquipment(
                any(com.lb_calc_web.domain.model.ControlConfiguration.class));

        service.configureFunctionalEquipment(domain);

        assertEquals(Map.of(display, 1), domain.getEquipment());
    }

    @Test
    void findById_shouldReturnMappedDomain() {
        LBCEntity entity = spy(LBCEntityMapper.toEntity(
                TestDataFactory.validLBC(null), equipment -> null));
        doReturn(26L).when(entity).getId();
        when(repository.findById(26L)).thenReturn(Optional.of(entity));

        assertEquals(26L, service.findById(26L).getId());
    }

    private List<com.lb_calc_web.domain.equipment.Equipment> configureValidEquipment(LBC domain) {
        var display = new com.lb_calc_web.domain.equipment.Equipment(
                7201L,
                com.lb_calc_web.domain.equipment.EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10,
                com.lb_calc_web.domain.equipment.EquipmentCategory.DISPLAY,
                "Дисплей 10", "", 300, 180, 30);
        var rfid = new com.lb_calc_web.domain.equipment.Equipment(
                7202L,
                com.lb_calc_web.domain.equipment.EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID,
                com.lb_calc_web.domain.equipment.EquipmentCategory.RFID_READER,
                "RFID-считыватель", "", 100, 50, 30);
        var configuration = domain.getControlConfiguration();
        configuration.setEquipment(Map.of(display, 1, rfid, 1));
        domain.setControlConfiguration(configuration);
        return List.of(display, rfid);
    }

    private void stubEquipment(List<com.lb_calc_web.domain.equipment.Equipment> models)
            throws Exception {
        Field idField = EquipmentEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        for (var model : models) {
            EquipmentEntity entity = EquipmentEntityMapper.toEntity(model);
            idField.set(entity, model.getId());
            when(equipmentRepository.findById(model.getId())).thenReturn(Optional.of(entity));
        }
    }

    @Test
    void saveLBC_shouldPersistNewConfiguration() throws Exception {
        LBC domain = TestDataFactory.validLBC(null);
        List<com.lb_calc_web.domain.equipment.Equipment> equipment = configureValidEquipment(domain);
        stubEquipment(equipment);
        LBCEntity saved = spy(LBCEntityMapper.toEntity(domain, equipmentModel -> equipmentRepository.findById(equipmentModel.getId()).orElseThrow()));
        doReturn(100L).when(saved).getId();
        when(repository.findCandidates(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), anyString(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), anyInt())).thenReturn(List.of());
        when(repository.save(any(LBCEntity.class))).thenReturn(saved);

        LBC result = service.saveLBC(domain);

        assertEquals(100L, result.getId());
        verify(lockService).acquire();
        verify(repository).save(any(LBCEntity.class));
    }

    @Test
    void saveLBC_shouldRejectInvalidGeometryBeforeSaving() {
        LBC source = TestDataFactory.validLBC(null);
        LBC domain = new LBC(
                10, source.getWidth(), source.getDepth(), source.getUpperFrame(),
                source.getBottomFrame(), source.getColorBody(), source.getColorDoor(),
                source.getTypeLb(), source.getCountCells(), source.getDirectionDoorOpening(),
                source.getDoorThickness(), source.getControlConfiguration(),
                TestDataFactory.createSizeBounds());
        configureValidEquipment(domain);

        assertThrows(ValidationSizeException.class, () -> service.saveLBC(domain));
        verify(repository, never()).save(any());
    }
}
