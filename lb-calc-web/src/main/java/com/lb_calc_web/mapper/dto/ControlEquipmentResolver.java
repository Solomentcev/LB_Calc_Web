package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.domain.equipment.EquipmentItem;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.domain.model.EquipmentConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Преобразует выбранные в DTO позиции оборудования в доменную конфигурацию. */
final class ControlEquipmentResolver {
    private ControlEquipmentResolver() { }

    static EquipmentConfiguration resolve(
            String displayName, Integer displayQuantity,
            String barReaderName, Integer barReaderQuantity,
            boolean printer, Integer printerQuantity,
            boolean rfidReader, Integer rfidReaderQuantity,
            String moduleType) {
        return resolve(displayName, displayQuantity, barReaderName, barReaderQuantity,
                printer, printerQuantity, rfidReader, rfidReaderQuantity,
                moduleType, EquipmentCatalog::resolve);
    }

    static EquipmentConfiguration resolve(
            String displayName, Integer displayQuantity,
            String barReaderName, Integer barReaderQuantity,
            boolean printer, Integer printerQuantity,
            boolean rfidReader, Integer rfidReaderQuantity,
            String moduleType, Function<String, Equipment> equipmentResolver) {
        int displayCount = requireQuantity(displayQuantity, "дисплея");
        int barReaderCount = requireQuantity(barReaderQuantity, "сканера штрихкода");
        int printerCount = requireQuantity(printerQuantity, "принтера");
        int rfidCount = requireQuantity(rfidReaderQuantity, "RFID-считывателя");
        validatePresence(printer, printerCount, "принтера");
        validatePresence(rfidReader, rfidCount, "RFID-считывателя");

        List<EquipmentItem> items = new ArrayList<>();
        addItem(items, displayName, displayCount, Display.NONE, moduleType, equipmentResolver);
        addItem(items, barReaderName, barReaderCount, BarReader.NONE, moduleType, equipmentResolver);
        addCatalogItem(items, Printer.CATALOG_POSITION_ID, printerCount, equipmentResolver);
        addCatalogItem(items, RfidReader.CATALOG_POSITION_ID, rfidCount, equipmentResolver);
        return EquipmentConfiguration.fromItems(items, List.of());
    }

    static EquipmentConfiguration resolve(
            List<EquipmentSelectionDTO> selections,
            Function<String, Equipment> equipmentResolver) {
        List<EquipmentItem> items = new ArrayList<>();
        for (EquipmentSelectionDTO selection : selections) {
            Equipment equipment = equipmentResolver.apply(selection.catalogPositionId());
            if (equipment == null) {
                throw new IllegalArgumentException(
                        "Позиция оборудования не найдена: " + selection.catalogPositionId());
            }
            items.add(new EquipmentItem(equipment, selection.quantity()));
        }
        return EquipmentConfiguration.fromItems(items, List.of());
    }

    private static int requireQuantity(Integer quantity, String name) {
        if (quantity == null) {
            throw new IllegalArgumentException("Количество " + name + " должно быть задано");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Количество оборудования не может быть отрицательным");
        }
        return quantity;
    }

    private static void validatePresence(boolean present, int quantity, String name) {
        if (present != (quantity > 0)) {
            throw new IllegalArgumentException(
                    "Признак наличия " + name + " не соответствует его количеству");
        }
    }

    private static void addItem(List<EquipmentItem> items, String idOrName, int quantity,
                                Equipment none, String moduleType,
                                Function<String, Equipment> equipmentResolver) {
        if (quantity == 0) return;
        if (idOrName == null || idOrName.isBlank()) {
            throw new IllegalArgumentException(
                    "Не указана позиция оборудования для количества " + quantity);
        }
        Equipment equipment = equipmentResolver.apply(idOrName);
        if (equipment == null || equipment.equals(none)) {
            throw new IllegalArgumentException(
                    "Некорректная позиция оборудования для модуля " + moduleType);
        }
        items.add(new EquipmentItem(equipment, quantity));
    }

    private static void addCatalogItem(List<EquipmentItem> items, String id, int quantity,
                                       Function<String, Equipment> equipmentResolver) {
        if (quantity == 0) return;
        Equipment equipment = equipmentResolver.apply(id);
        if (equipment == null) {
            throw new IllegalArgumentException("В каталоге отсутствует оборудование: " + id);
        }
        items.add(new EquipmentItem(equipment, quantity));
    }
}
