package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.repository.EquipmentRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Проверяет подбор физической комплектации из каталога оборудования. */
class EquipmentCatalogServiceTest {

    @Test
    void configureFunctionalEquipment_shouldSelectDefaultModelsForAllRequirements() throws Exception {
        EquipmentEntity display = equipment(
                801L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10.name(),
                EquipmentCategory.DISPLAY, "Дисплей 10", true);
        EquipmentEntity rfid = equipment(
                802L, EquipmentFunctionalType.ACCESS, AccessPurpose.RFID.name(),
                EquipmentCategory.RFID_READER, "RFID", true);

        EquipmentRepository repository = mock(EquipmentRepository.class);
        when(repository.findAll()).thenReturn(List.of(display, rfid));
        EquipmentCatalogService service = new EquipmentCatalogService(repository);

        ControlConfiguration configuration = new ControlConfiguration(
                new FunctionalConfiguration(
                        DisplayPurpose.INCH_10,
                        Set.of(AccessPurpose.RFID),
                        com.lb_calc_web.domain.equipment.PaymentPurpose.NONE,
                        com.lb_calc_web.domain.equipment.PrinterPurpose.ABSENT
                ),
                Map.of()
        );

        service.configureFunctionalEquipment(configuration);

        Map<Equipment, Integer> selected = configuration.getEquipment();
        assertEquals(2, selected.size());
        assertEquals(Set.of("Дисплей 10", "RFID"),
                selected.keySet().stream().map(Equipment::getName).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of(1), Set.copyOf(selected.values()));
        verify(repository).findAll();
    }

    private EquipmentEntity equipment(
            long id,
            EquipmentFunctionalType type,
            String purpose,
            EquipmentCategory category,
            String name,
            boolean defaultModel
    ) throws Exception {
        EquipmentEntity entity = EquipmentEntity.create();
        entity.setFunctionalType(type);
        entity.setPurpose(purpose);
        entity.setCategory(category);
        entity.setName(name);
        entity.setDescription("");
        entity.setWidth(100);
        entity.setHeight(100);
        entity.setDepth(50);
        entity.setDefaultModel(defaultModel);
        Field idField = EquipmentEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
        return entity;
    }
}
