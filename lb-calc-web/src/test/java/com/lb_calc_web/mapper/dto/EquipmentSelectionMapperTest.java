package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.CatalogEquipment;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentSelectionMapperTest {

    private final Map<String, CatalogEquipment> catalog = Map.of(
            "DISPLAY_LC17", equipment("DISPLAY_LC17", "DISPLAY", "LC17"),
            "DISPLAY_LC19", equipment("DISPLAY_LC19", "DISPLAY", "LC19"),
            "MODEM_CUSTOM", equipment("MODEM_CUSTOM", "MODEM", "Модем")
    );

    private final Function<String, com.lb_calc_web.domain.equipment.Equipment> resolver =
            value -> catalog.values().stream()
                    .filter(item -> item.getCatalogPositionId().equals(value)
                            || item.getName().equals(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Не найдена позиция " + value));

    @Test
    void mergeDisplaySelection_shouldPreserveOtherEquipmentAndReplaceDisplay() {
        List<EquipmentSelectionDTO> existing = List.of(
                new EquipmentSelectionDTO("DISPLAY_LC17", 1),
                new EquipmentSelectionDTO("MODEM_CUSTOM", 2)
        );

        List<EquipmentSelectionDTO> result = EquipmentSelectionMapper.mergeDisplaySelection(
                existing, "LC19", null, resolver);

        assertEquals(List.of(
                new EquipmentSelectionDTO("MODEM_CUSTOM", 2),
                new EquipmentSelectionDTO("DISPLAY_LC19", 1)
        ), result);
    }

    @Test
    void mergeDisplaySelection_shouldRemoveDisplayWhenNoneSelected() {
        List<EquipmentSelectionDTO> existing = List.of(
                new EquipmentSelectionDTO("DISPLAY_LC17", 1),
                new EquipmentSelectionDTO("MODEM_CUSTOM", 1)
        );

        List<EquipmentSelectionDTO> result = EquipmentSelectionMapper.mergeDisplaySelection(
                existing, "Без дисплея", null, resolver);

        assertEquals(List.of(new EquipmentSelectionDTO("MODEM_CUSTOM", 1)), result);
    }

    @Test
    void mergeDisplaySelection_shouldPreserveExistingDisplayWhenFormOmitsIt() {
        List<EquipmentSelectionDTO> existing = List.of(
                new EquipmentSelectionDTO("DISPLAY_LC17", 1),
                new EquipmentSelectionDTO("MODEM_CUSTOM", 1)
        );

        List<EquipmentSelectionDTO> result = EquipmentSelectionMapper.mergeDisplaySelection(
                existing, null, null, resolver);

        assertEquals(existing, result);
    }

    private CatalogEquipment equipment(String id, String category, String name) {
        return new CatalogEquipment(id, category, name, "", 0, 0, 0,
                0, 0, 0, false, false, false, false);
    }
}
