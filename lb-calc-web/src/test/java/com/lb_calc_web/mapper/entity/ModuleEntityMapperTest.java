package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleEntityMapperTest {

    private static EquipmentEntity unresolvedEquipment(Equipment equipment) {
        throw new AssertionError("В этом тесте не ожидается разрешение оборудования");
    }

    private static ModuleEntity toEntity(com.lb_calc_web.domain.model.Module module) {
        return ModuleEntityMapper.toEntity(module, ModuleEntityMapperTest::unresolvedEquipment);
    }

    @Test
    void toEntity_shouldCreateLBEntity() {
        var domain = TestDataFactory.validLB(null);
        ModuleEntity entity = toEntity(domain);
        assertInstanceOf(LBEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toEntity_shouldCreateLCEntity() {
        var domain = TestDataFactory.validLC(null);
        ModuleEntity entity = toEntity(domain);
        assertInstanceOf(LCEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toEntity_shouldCreateLBCEntity() {
        var domain = TestDataFactory.validLBC(null);
        ModuleEntity entity = toEntity(domain);
        assertInstanceOf(LBCEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldRejectStaleLcDescription() {
        LCEntity entity = (LCEntity) toEntity(TestDataFactory.validLC(null));
        entity.setDescription("Устаревшее описание");
        assertThrows(IllegalStateException.class, () -> ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldRejectStaleLbcCellDimensions() {
        LBCEntity entity = (LBCEntity) toEntity(TestDataFactory.validLBC(null));
        entity.setDepthCell(entity.getDepthCell() + 1);
        assertThrows(IllegalStateException.class, () -> ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldRejectStaleDerivedCellDimensions() {
        LBEntity entity = (LBEntity) toEntity(TestDataFactory.validLB(null));
        entity.setWidthCell(entity.getWidthCell() + 1);
        assertThrows(IllegalStateException.class, () -> ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void storageEntity_shouldRejectInvalidRequiredFields() {
        LBEntity entity = new LBEntity();
        assertThrows(NullPointerException.class, () -> entity.setType(null));
        assertThrows(IllegalArgumentException.class, () -> entity.setType(" "));
        assertThrows(NullPointerException.class, () -> entity.setDirectionDoorOpening(null));
        assertThrows(IllegalArgumentException.class, () -> entity.setCountCells(21));
        assertThrows(IllegalArgumentException.class, () -> entity.setCountCells(-1));
    }

    @Test
    void moduleEntity_shouldRejectMissingColors() {
        ModuleEntity entity = new LBEntity();
        assertThrows(NullPointerException.class, () -> entity.setColorBody(null));
        assertThrows(NullPointerException.class, () -> entity.setColorDoor(null));
    }

    @Test
    void toDomain_shouldReturnNullForNullEntity() {
        assertNull(ModuleEntityMapper.toDomain(null));
    }

    @Test
    void toEntity_shouldRejectNullDomain() {
        assertThrows(NullPointerException.class,
                () -> ModuleEntityMapper.toEntity(null, ModuleEntityMapperTest::unresolvedEquipment));
    }

    @Test
    void roundTrip_shouldPreserveConcreteModuleTypeAndConfiguration() {
        com.lb_calc_web.domain.model.Module[] modules = {
                TestDataFactory.validLB(null),
                TestDataFactory.validLC(null),
                TestDataFactory.validLBC(null)
        };
        for (var module : modules) {
            ModuleEntity entity = toEntity(module);
            var restored = ModuleEntityMapper.toDomain(entity);
            assertEquals(module.getClass(), restored.getClass());
            assertEquals(module, restored);
        }
    }
}
