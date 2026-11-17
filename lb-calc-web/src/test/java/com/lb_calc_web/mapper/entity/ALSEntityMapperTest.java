package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.entity.ALSEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ALSEntityMapperTest {

    @Test
    void roundTrip_shouldPreserveModuleCompositionAndPhysicalOrder() {
        ALS domain = TestDataFactory.validALS(null);

        ALSEntity entity = ALSEntityMapper.toEntity(
                domain,
                ModuleEntityMapper::toEntity
        );

        assertEquals(domain.getModules().size(), entity.getModules().size());

        for (int i = 0; i < entity.getModules().size(); i++) {
            assertEquals(i, entity.getModules().get(i).getModuleOrder());
        }

        ALS restored = ALSEntityMapper.toDomain(entity);

        assertEquals(domain, restored);
        assertEquals(domain.getModules(), restored.getModules());
    }

    @Test
    void toEntity_shouldResolveEachModuleExactlyOnce() {
        ALS domain = TestDataFactory.validALS(null);
        int[] resolvedModules = {0};

        ALSEntityMapper.toEntity(domain, module -> {
            resolvedModules[0]++;
            return ModuleEntityMapper.toEntity(module);
        });

        assertEquals(domain.getModules().size(), resolvedModules[0]);
    }

    @Test
    void toEntity_shouldRejectNullModuleResolverResult() {
        ALS domain = TestDataFactory.validALS(null);

        assertThrows(
                NullPointerException.class,
                () -> ALSEntityMapper.toEntity(domain, module -> null)
        );
    }
}
