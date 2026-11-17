package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.entity.ALSEntity;

import java.util.List;
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
    void updateEntity_shouldReuseUnchangedModuleAssociations() {
        ALS domain = TestDataFactory.validALS(null);
        ALSEntity entity = ALSEntityMapper.toEntity(
                domain,
                ModuleEntityMapper::toEntity
        );

        var originalAssociations = List.copyOf(entity.getModules());

        ALSEntityMapper.updateEntity(
                domain,
                entity,
                module -> originalAssociations.stream()
                        .map(com.lb_calc_web.entity.ALSModuleEntity::getModule)
                        .filter(existing -> ModuleEntityMapper.toDomain(existing).equals(module))
                        .findFirst()
                        .orElseThrow()
        );

        assertEquals(originalAssociations.size(), entity.getModules().size());
        for (int i = 0; i < originalAssociations.size(); i++) {
            assertSame(originalAssociations.get(i), entity.getModules().get(i));
        }
    }

    @Test
    void toDomain_shouldRejectStaleDerivedWidth() {
        ALSEntity entity = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(null),
                ModuleEntityMapper::toEntity
        );
        entity.setWidth(entity.getWidth() + 1);

        assertThrows(
                IllegalStateException.class,
                () -> ALSEntityMapper.toDomain(entity)
        );
    }

    @Test
    void updateEntity_shouldReplaceAssociationsWhenPhysicalOrderChanges() {
        ALSEntity entity = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(null),
                ModuleEntityMapper::toEntity
        );
        var originalAssociations = List.copyOf(entity.getModules());
        List<com.lb_calc_web.entity.ModuleEntity> reversedModules =
                originalAssociations.stream()
                        .map(com.lb_calc_web.entity.ALSModuleEntity::getModule)
                        .collect(java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                list -> {
                                    java.util.Collections.reverse(list);
                                    return list;
                                }
                        ));

        entity.synchronizeModules(reversedModules);

        assertEquals(originalAssociations.size(), entity.getModules().size());
        for (int i = 0; i < entity.getModules().size(); i++) {
            assertEquals(i, entity.getModules().get(i).getModuleOrder());
            assertNotSame(originalAssociations.get(i), entity.getModules().get(i));
        }
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
