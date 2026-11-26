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

        ALS restored = ALSEntityMapper.toDomain(entity, TestDataFactory.lcMinimumWidth(), TestDataFactory.lcDisplaySideClearance(),
                TestDataFactory.lcWidthRoundingStep(), TestDataFactory.lcDisplayCenterHeight(),
                TestDataFactory.lcDisplayTopClearance());

        assertEquals(domain, restored);
        assertEquals(domain.getModules(), restored.getModules());
    }

    @Test
    void roundTrip_shouldPersistAlsFunctionalConfigurationIndependentlyOfModuleConfiguration() {
        ALS domain = TestDataFactory.validALS(null);
        var expected = new com.lb_calc_web.domain.model.FunctionalConfiguration(
                com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_17,
                java.util.Set.of(
                        com.lb_calc_web.domain.equipment.AccessPurpose.RFID,
                        com.lb_calc_web.domain.equipment.AccessPurpose.QR_CODE
                ),
                com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS_AND_CASH,
                com.lb_calc_web.domain.equipment.PrinterPurpose.PRESENT
        );
        domain.setFunctionalConfiguration(expected);

        ALSEntity entity = ALSEntityMapper.toEntity(
                domain,
                ALSEntityMapperTest::toEntity
        );

        assertEquals(expected, entity.getFunctionalConfiguration());

        var independentAlsConfiguration =
                new com.lb_calc_web.domain.model.FunctionalConfiguration(
                        com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_19,
                        java.util.Set.of(com.lb_calc_web.domain.equipment.AccessPurpose.PIN),
                        com.lb_calc_web.domain.equipment.PaymentPurpose.CASH,
                        com.lb_calc_web.domain.equipment.PrinterPurpose.OTHER
                );
        entity.setFunctionalConfiguration(independentAlsConfiguration);

        ALS restored = ALSEntityMapper.toDomain(entity, TestDataFactory.lcMinimumWidth(), TestDataFactory.lcDisplaySideClearance(),
                TestDataFactory.lcWidthRoundingStep(), TestDataFactory.lcDisplayCenterHeight(),
                TestDataFactory.lcDisplayTopClearance());

        assertEquals(independentAlsConfiguration, restored.getFunctionalConfiguration());
        assertEquals(
                independentAlsConfiguration,
                restored.getControlModule().getControlConfiguration().getFunctionalConfiguration()
        );
    }

    @Test
    void updateEntity_shouldReuseUnchangedModuleAssociations() {
        ALS domain = TestDataFactory.validALS(null);
        ALSEntity entity = ALSEntityMapper.toEntity(
                domain,
                ALSEntityMapperTest::toEntity
        );

        var originalAssociations = List.copyOf(entity.getModules());

        ALSEntityMapper.updateEntity(
                domain,
                entity,
                module -> originalAssociations.stream()
                        .map(com.lb_calc_web.entity.ALSModuleEntity::getModule)
                        .filter(existing -> ModuleEntityMapper.toDomain(existing, TestDataFactory.lcMinimumWidth(), TestDataFactory.lcDisplaySideClearance(),
                TestDataFactory.lcWidthRoundingStep(), TestDataFactory.lcDisplayCenterHeight(),
                TestDataFactory.lcDisplayTopClearance()).equals(module))
                        .findFirst()
                        .orElseThrow()
        );

        assertEquals(originalAssociations.size(), entity.getModules().size());
        for (int i = 0; i < originalAssociations.size(); i++) {
            assertSame(originalAssociations.get(i), entity.getModules().get(i));
        }
    }

    @Test
    void toDomain_shouldPreserveStoredTextAndRecalculateDerivedWidth() {
        ALS expected = TestDataFactory.validALS(null);
        ALSEntity entity = ALSEntityMapper.toEntity(
                expected,
                ALSEntityMapperTest::toEntity
        );
        entity.setName("Устаревшее название");
        entity.setDescription("Устаревшее описание");
        entity.getModules().forEach(module -> {
            module.getModule().setName("Сохранённое название модуля");
            module.getModule().setDescription("Сохранённое описание модуля");
        });
        entity.setWidth(entity.getWidth() + 1);

        ALS restored = ALSEntityMapper.toDomain(entity, TestDataFactory.lcMinimumWidth(), TestDataFactory.lcDisplaySideClearance(),
                TestDataFactory.lcWidthRoundingStep(), TestDataFactory.lcDisplayCenterHeight(),
                TestDataFactory.lcDisplayTopClearance());

        assertEquals("Устаревшее название", restored.getName());
        assertEquals("Устаревшее описание", restored.getDescription());
        assertEquals(expected.getWidth(), restored.getWidth());
        assertTrue(restored.getModules().stream().allMatch(module ->
                "Сохранённое название модуля".equals(module.getName())
                        && "Сохранённое описание модуля".equals(module.getDescription())));
        assertEquals(expected, restored);
    }

    @Test
    void updateEntity_shouldReplaceAssociationsWhenPhysicalOrderChanges() {
        ALSEntity entity = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(null),
                ALSEntityMapperTest::toEntity
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
            return ModuleEntityMapper.toEntity(module, equipment -> {
                throw new AssertionError("В этом тесте не ожидается разрешение оборудования");
            });
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
