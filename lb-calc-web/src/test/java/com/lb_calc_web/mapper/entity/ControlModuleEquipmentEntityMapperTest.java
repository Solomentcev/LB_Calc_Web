package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.service.EquipmentEntityResolver;
import com.lb_calc_web.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControlModuleEquipmentEntityMapperTest {

    private static final FunctionalConfiguration FUNCTIONAL = new FunctionalConfiguration(
            DisplayPurpose.INCH_10,
            Set.of(AccessPurpose.RFID, AccessPurpose.BAR_CODE),
            PaymentPurpose.NONE,
            PrinterPurpose.ABSENT
    );

    private EquipmentEntity persisted(Equipment equipment) throws Exception {
        EquipmentEntity entity = EquipmentEntityMapper.toEntity(equipment);
        Field id = EquipmentEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(entity, equipment.getId());
        return entity;
    }

    private Equipment equipment(
            long id,
            EquipmentFunctionalType type,
            com.lb_calc_web.domain.equipment.EquipmentPurpose purpose,
            EquipmentCategory category,
            String name
    ) {
        return new Equipment(id, type, purpose, category, name, "", 100, 50, 30);
    }

    @Test
    void lc_shouldRoundTripFunctionalConfigurationAndEquipmentPositions() throws Exception {
        Equipment display = equipment(1, EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10, EquipmentCategory.DISPLAY, "Дисплей 10");
        Equipment rfid = equipment(2, EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID, EquipmentCategory.RFID_READER, "RFID");
        Equipment barcode = equipment(3, EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE, EquipmentCategory.BAR_READER, "Сканер штрихкода");
        Map<Equipment, Integer> positions = Map.of(display, 1, rfid, 2, barcode, 1);
        LC source = new LC(1940, 500, 500, 50, 50,
                com.lb_calc_web.domain.attributes.Colors.Blue,
                com.lb_calc_web.domain.attributes.Colors.White,
                new ControlConfiguration(FUNCTIONAL, positions));
        Map<Long, EquipmentEntity> entities = Map.of(
                1L, persisted(display), 2L, persisted(rfid), 3L, persisted(barcode));

        LCEntity entity = LCEntityMapper.toEntity(source, item -> entities.get(item.getId()));
        LC restored = LCEntityMapper.toDomain(entity);

        assertEquals(source, restored);
        assertEquals(FUNCTIONAL, entity.getFunctionalConfiguration());
        assertEquals(3, entity.getEquipment().size());
        assertEquals(2, entity.getEquipment().stream()
                .filter(item -> item.getEquipment().getId().equals(2L))
                .findFirst().orElseThrow().getQuantity());
    }

    @Test
    void lbc_shouldRoundTripFunctionalConfigurationAndEquipmentPositions() throws Exception {
        Equipment rfid = equipment(2, EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID, EquipmentCategory.RFID_READER, "RFID");
        var sourceBase = com.lb_calc_web.TestDataFactory.validLBC(null);
        LBC source = new LBC(
                sourceBase.getHeight(), sourceBase.getWidth(), sourceBase.getDepth(),
                sourceBase.getUpperFrame(), sourceBase.getBottomFrame(),
                sourceBase.getColorBody(), sourceBase.getColorDoor(), sourceBase.getTypeLb(),
                sourceBase.getCountCells(), sourceBase.getDirectionDoorOpening(),
                sourceBase.getDoorThickness(),
                new ControlConfiguration(FUNCTIONAL, Map.of(rfid, 2))
        );
        EquipmentEntity persisted = persisted(rfid);

        LBCEntity entity = LBCEntityMapper.toEntity(source, item -> persisted);
        LBC restored = LBCEntityMapper.toDomain(entity);

        assertEquals(source, restored);
        assertEquals(FUNCTIONAL, entity.getFunctionalConfiguration());
        assertEquals(2, entity.getEquipment().iterator().next().getQuantity());
    }

    @Test
    void equipmentResolver_shouldReturnExistingManagedEntityByDomainId() throws Exception {
        Equipment equipment = equipment(44, EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID, EquipmentCategory.RFID_READER, "RFID");
        EquipmentEntity entity = persisted(equipment);
        EquipmentRepository repository = mock(EquipmentRepository.class);
        when(repository.findById(44L)).thenReturn(java.util.Optional.of(entity));

        EquipmentEntityResolver resolver = new EquipmentEntityResolver(repository);

        assertSame(entity, resolver.resolve(equipment));
        verify(repository).findById(44L);
    }

    @Test
    void equipmentResolver_shouldRejectUnknownCatalogId() {
        EquipmentRepository repository = mock(EquipmentRepository.class);
        when(repository.findById(45L)).thenReturn(java.util.Optional.empty());
        EquipmentEntityResolver resolver = new EquipmentEntityResolver(repository);

        assertThrows(java.util.NoSuchElementException.class,
                () -> resolver.resolveDomain(45L));
        verify(repository).findById(45L);
    }

    @Test
    void equipmentResolver_shouldRejectUnsavedDomainEquipment() {
        Equipment equipment = equipment(0, EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID, EquipmentCategory.RFID_READER, "RFID");
        EquipmentRepository repository = mock(EquipmentRepository.class);
        EquipmentEntityResolver resolver = new EquipmentEntityResolver(repository);

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(equipment));
        verifyNoInteractions(repository);
    }
}
