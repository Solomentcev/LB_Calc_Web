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
        assertEquals(domain, ModuleEntityMapper.toDomain(entity, TestDataFactory.lcGeometrySettings()));
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
    void toDomain_shouldPreserveStoredLcText() {
        var expected = TestDataFactory.validLC(null);
        LCEntity entity = (LCEntity) toEntity(expected);
        entity.setName("Устаревшее наименование");
        entity.setDescription("Устаревшее описание");

        var restored = ModuleEntityMapper.toDomain(entity);

        assertEquals("Устаревшее наименование", restored.getName());
        assertEquals("Устаревшее описание", restored.getDescription());
    }

    @Test
    void toDomain_shouldPreserveStoredLbcTextAndRecalculateCellDimensions() {
        var expected = TestDataFactory.validLBC(null);
        LBCEntity entity = (LBCEntity) toEntity(expected);
        entity.setName("Устаревшее наименование");
        entity.setDescription("Устаревшее описание");
        entity.setHeightCell(entity.getHeightCell() + 1);
        entity.setWidthCell(entity.getWidthCell() + 1);
        entity.setDepthCell(entity.getDepthCell() + 1);

        var restored = ModuleEntityMapper.toDomain(entity);

        assertEquals("Устаревшее наименование", restored.getName());
        assertEquals("Устаревшее описание", restored.getDescription());
        assertEquals(expected.getCellHeight(), ((com.lb_calc_web.domain.model.LBC) restored).getCellHeight());
        assertEquals(expected.getCellWidth(), ((com.lb_calc_web.domain.model.LBC) restored).getCellWidth());
        assertEquals(expected.getCellDepth(), ((com.lb_calc_web.domain.model.LBC) restored).getCellDepth());
    }

    @Test
    void toDomain_shouldPreserveStoredLbTextAndRecalculateCellDimensions() {
        var expected = TestDataFactory.validLB(null);
        LBEntity entity = (LBEntity) toEntity(expected);
        entity.setName("Устаревшее наименование");
        entity.setDescription("Устаревшее описание");
        entity.setHeightCell(entity.getHeightCell() + 1);
        entity.setWidthCell(entity.getWidthCell() + 1);
        entity.setDepthCell(entity.getDepthCell() + 1);

        var restored = ModuleEntityMapper.toDomain(entity);

        assertEquals("Устаревшее наименование", restored.getName());
        assertEquals("Устаревшее описание", restored.getDescription());
        assertEquals(expected.getCellHeight(), ((com.lb_calc_web.domain.model.LB) restored).getCellHeight());
        assertEquals(expected.getCellWidth(), ((com.lb_calc_web.domain.model.LB) restored).getCellWidth());
        assertEquals(expected.getCellDepth(), ((com.lb_calc_web.domain.model.LB) restored).getCellDepth());
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
        assertNull(ModuleEntityMapper.toDomain(null, TestDataFactory.lcGeometrySettings()));
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
