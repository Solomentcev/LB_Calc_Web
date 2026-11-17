package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleEntityMapperTest {

    @Test
    void toEntity_shouldCreateLBEntity() {
        LB domain = TestDataFactory.validLB(null);

        ModuleEntity entity = ModuleEntityMapper.toEntity(domain);

        assertInstanceOf(LBEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toEntity_shouldCreateLCEntity() {
        LC domain = TestDataFactory.validLC(null);

        ModuleEntity entity = ModuleEntityMapper.toEntity(domain);

        assertInstanceOf(LCEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toEntity_shouldCreateLBCEntity() {
        LBC domain = TestDataFactory.validLBC(null);

        ModuleEntity entity = ModuleEntityMapper.toEntity(domain);

        assertInstanceOf(LBCEntity.class, entity);
        assertEquals(domain, ModuleEntityMapper.toDomain(entity));
    }

    @Test
    void toDomain_shouldRejectStaleLcDescription() {
        LCEntity entity = (LCEntity) ModuleEntityMapper.toEntity(
                TestDataFactory.validLC(null)
        );
        entity.setDescription("Устаревшее описание");

        assertThrows(
                IllegalStateException.class,
                () -> ModuleEntityMapper.toDomain(entity)
        );
    }

    @Test
    void toDomain_shouldRejectStaleLbcCellDimensions() {
        LBCEntity entity = (LBCEntity) ModuleEntityMapper.toEntity(
                TestDataFactory.validLBC(null)
        );
        entity.setDepthCell(entity.getDepthCell() + 1);

        assertThrows(
                IllegalStateException.class,
                () -> ModuleEntityMapper.toDomain(entity)
        );
    }

    @Test
    void toDomain_shouldRejectStaleDerivedCellDimensions() {
        LBEntity entity = (LBEntity) ModuleEntityMapper.toEntity(
                TestDataFactory.validLB(null)
        );
        entity.setWidthCell(entity.getWidthCell() + 1);

        assertThrows(
                IllegalStateException.class,
                () -> ModuleEntityMapper.toDomain(entity)
        );
    }

    @Test
    void toDomain_shouldReturnNullForNullEntity() {
        assertNull(ModuleEntityMapper.toDomain(null));
    }

    @Test
    void toEntity_shouldRejectNullDomain() {
        assertThrows(
                NullPointerException.class,
                () -> ModuleEntityMapper.toEntity(null)
        );
    }

    @Test
    void roundTrip_shouldPreserveConcreteModuleTypeAndConfiguration() {
        Module[] modules = {
                TestDataFactory.validLB(null),
                TestDataFactory.validLC(null),
                TestDataFactory.validLBC(null)
        };

        for (Module module : modules) {
            ModuleEntity entity = ModuleEntityMapper.toEntity(module);
            Module restored = ModuleEntityMapper.toDomain(entity);

            assertEquals(module.getClass(), restored.getClass());
            assertEquals(module, restored);
        }
    }
}
