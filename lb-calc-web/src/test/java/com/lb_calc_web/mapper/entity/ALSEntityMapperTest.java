package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.ModuleEntity;

import java.lang.reflect.Field;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ALSEntityMapperTest {

    private static com.lb_calc_web.entity.ModuleEntity toEntity(
            com.lb_calc_web.domain.model.Module module
    ) {
        return ModuleEntityMapper.toEntity(module, equipment -> {
            throw new AssertionError("В этом тесте не ожидается разрешение оборудования");
        });
    }

    @Test
    void roundTrip_shouldPreserveModuleCompositionAndPhysicalOrder() {
        ALS domain = TestDataFactory.validALS(null);

        ALSEntity entity = ALSEntityMapper.toEntity(
                domain,
                ALSEntityMapperTest::toEntity
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
        List<com.lb_calc_web.entity.ModuleEntity> reorderedModules = List.of(
                originalAssociations.get(1).getModule(),
                originalAssociations.get(0).getModule(),
                originalAssociations.get(2).getModule()
        );

        entity.synchronizeModules(reorderedModules);

        assertEquals(originalAssociations.size(), entity.getModules().size());
        for (int i = 0; i < entity.getModules().size(); i++) {
            assertEquals(i, entity.getModules().get(i).getModuleOrder());
        }
        assertNotSame(originalAssociations.get(0), entity.getModules().get(0));
        assertNotSame(originalAssociations.get(1), entity.getModules().get(1));
        assertSame(originalAssociations.get(2), entity.getModules().get(2));
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
    @Test
    void synchronizeModules_shouldReuseAssociationWhenModuleIsProxySubclass() throws Exception {
        ALSEntity entity = new ALSEntity();
        LBEntity storedModule = new LBEntity();
        LBEntity proxyModule = new ModuleProxy();
        setModuleId(storedModule, 17L);
        setModuleId(proxyModule, 17L);
        entity.addModule(storedModule, 0);

        var originalAssociation = entity.getModules().get(0);
        entity.synchronizeModules(List.of(proxyModule));

        assertSame(originalAssociation, entity.getModules().get(0));
    }

    private static void setModuleId(ModuleEntity module, Long id) throws Exception {
        Field field = ModuleEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(module, id);
    }

    private static class ModuleProxy extends LBEntity {
    }

}
