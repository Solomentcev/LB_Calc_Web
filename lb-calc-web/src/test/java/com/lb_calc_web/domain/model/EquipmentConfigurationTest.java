package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.RfidReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Проверяет семантику и стабильное представление комплектации оборудования.
 */
class EquipmentConfigurationTest {

    @Test
    void getAllItems_shouldUseStableOrderRegardlessOfGroupAndInsertionOrder() {
        EquipmentConfiguration first = new EquipmentConfiguration(
                List.of(Display.LC10, new RfidReader()),
                List.of(BarReader.READER_1D)
        );
        EquipmentConfiguration second = new EquipmentConfiguration(
                List.of(BarReader.READER_1D),
                List.of(new RfidReader(), Display.LC10)
        );

        List<String> firstIds = first.getAllItems().stream()
                .map(item -> item.equipment().getCatalogPositionId())
                .toList();
        List<String> secondIds = second.getAllItems().stream()
                .map(item -> item.equipment().getCatalogPositionId())
                .toList();

        assertEquals(firstIds, secondIds);
        assertEquals(
                first.getAllItems().stream()
                        .map(item -> item.equipment().getCatalogPositionId())
                        .sorted()
                        .toList(),
                firstIds
        );
    }
}
